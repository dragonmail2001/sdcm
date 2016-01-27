package org.cloudy.dscm.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CLogger {
	private Timer timer;
	private ByteBuf buffer;
    private final PooledByteBufAllocator buffPool =
            new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());
    private ExecutorService threadPool;
    private Lock lock = new ReentrantLock(true);
    
    public void log(String str) {
    	try{
    		lock.lock();    	
	    	if(str == null) {
	    		buffer.writeBytes("null".getBytes());
	    	}else{
	    		buffer.writeBytes(str.getBytes());
	    	}
	    	buffer.writeChar('\n');
    	}finally{
    		lock.unlock();
    	}
    	
    	if(needDump()) {
	    	threadPool.execute(new Runnable() {
				public void run() {
	    			writeWithMappedByteBuffer();
				}
	    	});
    	}
    }
    
    public void log(StringBuilder str) {
    	try{
    		lock.lock();
	    	if(str == null) {
	    		buffer.writeBytes("null".getBytes());
	    	}else{
	    		buffer.writeBytes(str.toString().getBytes());
	    	}
	    	buffer.writeChar('\n');
    	}finally{
    		lock.unlock();
    	}
    	
    	if(needDump()) {
	    	threadPool.execute(new Runnable() {
				public void run() {
	    			writeWithMappedByteBuffer();
				}
	    	});
    	}
    }
	
    /**
     * 
     * @param capaticy  缓存大小
     * @param delay     timer执行时间
     * @param fmax	           日志文件最大字节数
     * @param lmax	 	 内存最大缓存字节数
     * @param pool	            线程池最大数量
     * @param path      日志文件路径
     * @throws IOException
     */
	public CLogger(int capaticy, long delay, long fmax, long lmax, int pool, String path, String pkey) throws IOException {
		this.path = path;
		this.fmax = fmax;
		this.lmax = lmax;
		this.pkey = pkey;
		this.curr = createFile();
		this.timer = new Timer();
		this.buffer = buffPool.directBuffer(capaticy);
		
		threadPool = Executors.newFixedThreadPool(pool);
		this.timer.schedule(new TimerTask() {
			@Override
			public void run() {
				writeWithMappedByteBuffer();
			}
		}, 0, delay);
	}
	
	private String path;
	private String pkey;
	private String curr;
	private long   flen;
	private long   fmax;
	private long   lmax;
	private FileChannel fileChannel;
	private RandomAccessFile raFile;
	private MappedByteBuffer mbbuffer;
	
	private String createFile() throws IOException {
		if(fileChannel != null) {
	        fileChannel.close();
		}
		
		if(raFile != null) {
			raFile.close();
		}
		
		curr = new StringBuilder(path).append(File.separator).append(pkey).
				append(date()).append(".log").toString();
		raFile = new RandomAccessFile(new File(curr), "rw");
		fileChannel = raFile.getChannel();
		flen = 0;
		
		return curr;
	}
	
	private String date() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.SSS");
		return format.format(new Date());
	}
	
	private boolean needDump() {
		try {
			lock.lock();
			return (buffer.readableBytes() > lmax);
		}finally{
			lock.unlock();
		}		
	}

	private void writeWithMappedByteBuffer() {
		try {
			lock.lock();
			
			int len = buffer.readableBytes();
			if(len <= 0) return;
	
			byte[] buff = new byte[len];
			buffer.getBytes(0, buff, 0, len);
			
			buffer.resetWriterIndex();
			buffer.resetReaderIndex();
	        
	    	mbbuffer = fileChannel.map(MapMode.READ_WRITE, raFile.length(), len);
	        mbbuffer.put(buff);
	        flen += len;
	        
	        if (flen > fmax) {
	        	unmap(mbbuffer);
	        	createFile();
	        } 	        
	        
		}catch(Exception exc) {
			exc.printStackTrace();
		}finally{
			lock.unlock();
		}
    }
	
	private void unmap(final MappedByteBuffer mappedByteBuffer) {
        if (mappedByteBuffer == null) {
            return;
        }
         
        mappedByteBuffer.force();
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Method getCleanerMethod = mappedByteBuffer.getClass()
                            .getMethod("cleaner", new Class[0]);
                    getCleanerMethod.setAccessible(true);
                    sun.misc.Cleaner cleaner = 
                            (sun.misc.Cleaner) getCleanerMethod
                                .invoke(mappedByteBuffer, new Object[0]);
                    cleaner.clean();
                    return null;
                } catch (Exception exc) {
                	throw new RuntimeException(exc.getCause());
                }
            }
        });
    }
}
