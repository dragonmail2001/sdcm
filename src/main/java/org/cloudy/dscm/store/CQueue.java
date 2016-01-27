package org.cloudy.dscm.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cloudy.dscm.common.CUtil;
import org.cloudy.dscm.publisher.CServer;

public class CQueue {
    // 每次触发删除文件，最多删除多少个文件
    private static final int DeleteFilesBatchMax = 10;
    // 文件存储位置
    private final String storePath;
    // 每个文件的大小
    private final int mapedFileSize;
    // 各个文件
    private final List<CMapedFile> mapedFiles = new ArrayList<CMapedFile>();
    // 读写锁（针对mapedFiles）
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
//    // 预分配MapedFile对象服务
//    private final AllocateMapedFileService allocateMapedFileService;
    // 刷盘刷到哪里
    private long committedWhere = 0;
    // 最后一条消息存储时间
    private volatile long storeTimestamp = 0;


//    public CQueue(final String storePath, int mapedFileSize,
//            AllocateMapedFileService allocateMapedFileService) {
//        this.storePath = storePath;
//        this.mapedFileSize = mapedFileSize;
//        this.allocateMapedFileService = allocateMapedFileService;
//    }
    public CQueue(final String storePath, int mapedFileSize) {
        this.storePath = storePath;
        this.mapedFileSize = mapedFileSize;
    }


    public CMapedFile getMapedFileByTime(final long timestamp) {
        Object[] mfs = this.copyMapedFiles(0);

        if (null == mfs)
            return null;

        for (int i = 0; i < mfs.length; i++) {
        	CMapedFile mapedFile = (CMapedFile) mfs[i];
            if (mapedFile.getLastModifiedTimestamp() >= timestamp) {
                return mapedFile;
            }
        }

        return (CMapedFile) mfs[mfs.length - 1];
    }


    private Object[] copyMapedFiles(final int reservedMapedFiles) {
        Object[] mfs = null;

        try {
            this.readWriteLock.readLock().lock();
            if (this.mapedFiles.size() <= reservedMapedFiles) {
                return null;
            }

            mfs = this.mapedFiles.toArray();
        }
        catch (Exception exc) {
            CServer.logger().log(exc.getMessage());
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }
        return mfs;
    }


    /**
     * recover时调用，不需要加锁
     */
    public void truncateDirtyFiles(long offset) {
        List<CMapedFile> willRemoveFiles = new ArrayList<CMapedFile>();

        for (CMapedFile file : this.mapedFiles) {
            long fileTailOffset = file.getFileFromOffset() + this.mapedFileSize;
            if (fileTailOffset > offset) {
                if (offset >= file.getFileFromOffset()) {
                    file.setWrotePostion((int) (offset % this.mapedFileSize));
                    file.setCommittedPosition((int) (offset % this.mapedFileSize));
                }
                else {
                    // 将文件删除掉
                    file.destroy(1000);
                    willRemoveFiles.add(file);
                }
            }
        }

        this.deleteExpiredFile(willRemoveFiles);
    }


    /**
     * 删除文件只能从头开始删
     */
    private void deleteExpiredFile(List<CMapedFile> files) {
        if (!files.isEmpty()) {
            try {
                this.readWriteLock.writeLock().lock();
                for (CMapedFile file : files) {
                    if (!this.mapedFiles.remove(file)) {
                        CServer.logger().log("deleteExpiredFile remove failed.");
                        break;
                    }
                }
            }
            catch (Exception exc) {
            	CServer.logger().log(exc.getMessage());
            }
            finally {
                this.readWriteLock.writeLock().unlock();
            }
        }
    }


    public boolean load() {
        File dir = new File(this.storePath);
        File[] files = dir.listFiles();
        if (files != null) {
            // ascending order
            Arrays.sort(files);
            for (File file : files) {
                // 校验文件大小是否匹配
                if (file.length() != this.mapedFileSize) {
                	CServer.logger().log(file + "\t" + file.length()
                            + " length not matched message store config value, ignore it");
                    return true;
                }

                // 恢复队列
                try {
                	CMapedFile mapedFile = new CMapedFile(file.getPath(), mapedFileSize);

                    mapedFile.setWrotePostion(this.mapedFileSize);
                    mapedFile.setCommittedPosition(this.mapedFileSize);
                    this.mapedFiles.add(mapedFile);
                    CServer.logger().log("load " + file.getPath() + " OK");
                }
                catch (IOException e) {
                	CServer.logger().log("load file " + file + " error" + e.getMessage());
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * 刷盘进度落后了多少
     */
    public long howMuchFallBehind() {
        if (this.mapedFiles.isEmpty())
            return 0;

        long committed = this.committedWhere;
        if (committed != 0) {
            CMapedFile mapedFile = this.getLastMapedFile();
            if (mapedFile != null) {
                return (mapedFile.getFileFromOffset() + mapedFile.getWrotePostion()) - committed;
            }
        }

        return 0;
    }


    public CMapedFile getLastMapedFile() {
        return this.getLastMapedFile(0);
    }


    /**
     * 获取最后一个MapedFile对象，如果一个都没有，则新创建一个，如果最后一个写满了，则新创建一个
     * 
     * @param startOffset
     *            如果创建新的文件，起始offset
     * @return
     */
    public CMapedFile getLastMapedFile(final long startOffset) {
        long createOffset = -1;
        CMapedFile mapedFileLast = null;
        {
            this.readWriteLock.readLock().lock();
            if (this.mapedFiles.isEmpty()) {
                createOffset = startOffset - (startOffset % this.mapedFileSize);
            }
            else {
                mapedFileLast = this.mapedFiles.get(this.mapedFiles.size() - 1);
            }
            this.readWriteLock.readLock().unlock();
        }

        if (mapedFileLast != null && mapedFileLast.isFull()) {
            createOffset = mapedFileLast.getFileFromOffset() + this.mapedFileSize;
        }

        if (createOffset != -1) {
            String nextFilePath = this.storePath + File.separator + CUtil.offset2FileName(createOffset);
//            String nextNextFilePath =
//                    this.storePath + File.separator
//                            + CUtil.offset2FileName(createOffset + this.mapedFileSize);
            CMapedFile mapedFile = null;

//            if (this.allocateMapedFileService != null) {
//                mapedFile =
//                        this.allocateMapedFileService.putRequestAndReturnMapedFile(nextFilePath,
//                            nextNextFilePath, this.mapedFileSize);
//            }
//            else {
                try {
                    mapedFile = new CMapedFile(nextFilePath, this.mapedFileSize);
                }
                catch (IOException e) {
                	CServer.logger().log("create mapedfile exception" + e.getMessage());
                }
//            }

            if (mapedFile != null) {
                this.readWriteLock.writeLock().lock();
                if (this.mapedFiles.isEmpty()) {
                    mapedFile.setFirstCreateInQueue(true);
                }
                this.mapedFiles.add(mapedFile);
                this.readWriteLock.writeLock().unlock();
            }

            return mapedFile;
        }

        return mapedFileLast;
    }


    /**
     * 获取队列的最小Offset，如果队列为空，则返回-1
     */
    public long getMinOffset() {
        try {
            this.readWriteLock.readLock().lock();
            if (!this.mapedFiles.isEmpty()) {
                return this.mapedFiles.get(0).getFileFromOffset();
            }
        }
        catch (Exception e) {
        	CServer.logger().log("getMinOffset has exception." + e.getMessage());
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        return -1;
    }


    public long getMaxOffset() {
        try {
            this.readWriteLock.readLock().lock();
            if (!this.mapedFiles.isEmpty()) {
                int lastIndex = this.mapedFiles.size() - 1;
                CMapedFile mapedFile = this.mapedFiles.get(lastIndex);
                return mapedFile.getFileFromOffset() + mapedFile.getWrotePostion();
            }
        }
        catch (Exception e) {
        	CServer.logger().log("getMinOffset has exception." + e.getMessage());
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        return 0;
    }


    /**
     * 恢复时调用
     */
    public void deleteLastMapedFile() {
        if (!this.mapedFiles.isEmpty()) {
            int lastIndex = this.mapedFiles.size() - 1;
            CMapedFile mapedFile = this.mapedFiles.get(lastIndex);
            mapedFile.destroy(1000);
            this.mapedFiles.remove(mapedFile);
            CServer.logger().log("on recover, destroy a logic maped file " + mapedFile.getFileName());
        }
    }


    /**
     * 根据文件过期时间来删除物理队列文件
     */
    public int deleteExpiredFileByTime(//
            final long expiredTime, //
            final int deleteFilesInterval, //
            final long intervalForcibly,//
            final boolean cleanImmediately//
    ) {
        Object[] mfs = this.copyMapedFiles(0);

        if (null == mfs)
            return 0;

        // 最后一个文件处于写状态，不能删除
        int mfsLength = mfs.length - 1;
        int deleteCount = 0;
        List<CMapedFile> files = new ArrayList<CMapedFile>();
        if (null != mfs) {
            for (int i = 0; i < mfsLength; i++) {
            	CMapedFile mapedFile = (CMapedFile) mfs[i];
                long liveMaxTimestamp = mapedFile.getLastModifiedTimestamp() + expiredTime;
                if (System.currentTimeMillis() >= liveMaxTimestamp//
                        || cleanImmediately) {
                    if (mapedFile.destroy(intervalForcibly)) {
                        files.add(mapedFile);
                        deleteCount++;

                        if (files.size() >= DeleteFilesBatchMax) {
                            break;
                        }

                        if (deleteFilesInterval > 0 && (i + 1) < mfsLength) {
                            try {
                                Thread.sleep(deleteFilesInterval);
                            }
                            catch (InterruptedException e) {
                            }
                        }
                    }
                    else {
                        break;
                    }
                }
            }
        }

        deleteExpiredFile(files);

        return deleteCount;
    }


//    /**
//     * 根据物理队列最小Offset来删除逻辑队列
//     * 
//     * @param offset
//     *            物理队列最小offset
//     */
//    public int deleteExpiredFileByOffset(long offset, int unitSize) {
//        Object[] mfs = this.copyMapedFiles(0);
//
//        List<CMapedFile> files = new ArrayList<CMapedFile>();
//        int deleteCount = 0;
//        if (null != mfs) {
//            // 最后一个文件处于写状态，不能删除
//            int mfsLength = mfs.length - 1;
//
//            // 这里遍历范围 0 ... last - 1
//            for (int i = 0; i < mfsLength; i++) {
//                boolean destroy = true;
//                CMapedFile mapedFile = (CMapedFile) mfs[i];
//                SelectMapedBufferResult result = mapedFile.selectMapedBuffer(this.mapedFileSize - unitSize);
//                if (result != null) {
//                    long maxOffsetInLogicQueue = result.getByteBuffer().getLong();
//                    result.release();
//                    // 当前文件是否可以删除
//                    destroy = (maxOffsetInLogicQueue < offset);
//                    if (destroy) {
//                    	CServer.logger().log("physic min offset " + offset + ", logics in current mapedfile max offset "
//                                + maxOffsetInLogicQueue + ", delete it");
//                    }
//                }
//                else {
//                	CServer.logger().log("this being not excuted forever.");
//                    break;
//                }
//
//                if (destroy && mapedFile.destroy(1000 * 60)) {
//                    files.add(mapedFile);
//                    deleteCount++;
//                }
//                else {
//                    break;
//                }
//            }
//        }
//
//        deleteExpiredFile(files);
//
//        return deleteCount;
//    }


    /**
     * 返回值表示是否全部刷盘完成
     * 
     * @return
     */
    public boolean commit(final int flushLeastPages) {
        boolean result = true;
        CMapedFile mapedFile = this.findMapedFileByOffset(this.committedWhere, true);
        if (mapedFile != null) {
            long tmpTimeStamp = mapedFile.getStoreTimestamp();
            int offset = mapedFile.commit(flushLeastPages);
            long where = mapedFile.getFileFromOffset() + offset;
            result = (where == this.committedWhere);
            this.committedWhere = where;
            if (0 == flushLeastPages) {
                this.storeTimestamp = tmpTimeStamp;
            }
        }

        return result;
    }


    public CMapedFile findMapedFileByOffset(final long offset, final boolean returnFirstOnNotFound) {
        try {
            this.readWriteLock.readLock().lock();
            CMapedFile mapedFile = this.getFirstMapedFile();

            if (mapedFile != null) {
                int index =
                        (int) ((offset / this.mapedFileSize) - (mapedFile.getFileFromOffset() / this.mapedFileSize));
                if (index < 0 || index >= this.mapedFiles.size()) {
                	CServer.logger().log(String.format(
                            "findMapedFileByOffset offset not matched, request Offset: {}, index: {}, mapedFileSize: {}, mapedFiles count: {}, StackTrace: {}",//
                            offset,//
                            index,//
                            this.mapedFileSize,//
                            this.mapedFiles.size(),//
                            CUtil.currentStackTrace()));
                }

                try {
                    return this.mapedFiles.get(index);
                }
                catch (Exception e) {
                    if (returnFirstOnNotFound) {
                        return mapedFile;
                    }
                }
            }
        }
        catch (Exception e) {
        	CServer.logger().log("findMapedFileByOffset Exception"+e.getMessage());
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }

        return null;
    }


    private CMapedFile getFirstMapedFile() {
        if (this.mapedFiles.isEmpty()) {
            return null;
        }

        return this.mapedFiles.get(0);
    }


    public CMapedFile getLastMapedFile2() {
        if (this.mapedFiles.isEmpty()) {
            return null;
        }
        return this.mapedFiles.get(this.mapedFiles.size() - 1);
    }


    public CMapedFile findMapedFileByOffset(final long offset) {
        return findMapedFileByOffset(offset, false);
    }


    public long getMapedMemorySize() {
        long size = 0;

        Object[] mfs = this.copyMapedFiles(0);
        if (mfs != null) {
            for (Object mf : mfs) {
                if (((CReference) mf).isAvailable()) {
                    size += this.mapedFileSize;
                }
            }
        }

        return size;
    }


    public boolean retryDeleteFirstFile(final long intervalForcibly) {
    	CMapedFile mapedFile = this.getFirstMapedFileOnLock();
        if (mapedFile != null) {
            if (!mapedFile.isAvailable()) {
            	CServer.logger().log("the mapedfile was destroyed once, but still alive, " + mapedFile.getFileName());
                boolean result = mapedFile.destroy(intervalForcibly);
                if (result) {
                	CServer.logger().log("the mapedfile redelete OK, " + mapedFile.getFileName());
                    List<CMapedFile> tmps = new ArrayList<CMapedFile>();
                    tmps.add(mapedFile);
                    this.deleteExpiredFile(tmps);
                }
                else {
                	CServer.logger().log("the mapedfile redelete Failed, " + mapedFile.getFileName());
                }

                return result;
            }
        }

        return false;
    }


    public CMapedFile getFirstMapedFileOnLock() {
        try {
            this.readWriteLock.readLock().lock();
            return this.getFirstMapedFile();
        }
        finally {
            this.readWriteLock.readLock().unlock();
        }
    }


    /**
     * 关闭队列，队列数据还在，但是不能访问
     */
    public void shutdown(final long intervalForcibly) {
        this.readWriteLock.readLock().lock();
        for (CMapedFile mf : this.mapedFiles) {
            mf.shutdown(intervalForcibly);
        }
        this.readWriteLock.readLock().unlock();
    }


    /**
     * 销毁队列，队列数据被删除，此函数有可能不成功
     */
    public void destroy() {
        this.readWriteLock.writeLock().lock();
        for (CMapedFile mf : this.mapedFiles) {
            mf.destroy(1000 * 3);
        }
        this.mapedFiles.clear();
        this.committedWhere = 0;

        // delete parent directory
        File file = new File(storePath);
        if (file.isDirectory()) {
            file.delete();
        }
        this.readWriteLock.writeLock().unlock();
    }


    public long getCommittedWhere() {
        return committedWhere;
    }


    public void setCommittedWhere(long committedWhere) {
        this.committedWhere = committedWhere;
    }


    public long getStoreTimestamp() {
        return storeTimestamp;
    }


    public List<CMapedFile> getMapedFiles() {
        return mapedFiles;
    }


    public int getMapedFileSize() {
        return mapedFileSize;
    }
}
