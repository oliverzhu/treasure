package com.aliyun.android.oss.task;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

import com.cloud.client.file.MissionListener;

public class CustomInputStreamEntity extends InputStreamEntity {

    private MissionListener listener;
    public CountingOutputStream countingOutputStream;

    public CustomInputStreamEntity(InputStream instream, long length) {
        super(instream, length);
    }

    public void setProgressListener(MissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        countingOutputStream = new CountingOutputStream(outstream, listener);
        super.writeTo(countingOutputStream);
    }

    public static class CountingOutputStream extends FilterOutputStream {
        
        private MissionListener listener = null;
        private long transferred;
        
        public long getTransferred() {
            return transferred;
        }
    
        public void setTransferred(long transferred) {
            this.transferred = transferred;
        }
    
        public CountingOutputStream(final OutputStream out,
                final MissionListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }
    
        @Override
        public void write(byte[] buffer) throws IOException {
            super.write(buffer);
            this.transferred += buffer.length;
            if (this.listener != null) {
                this.listener.transferred(this.transferred);
            }
        }
    
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            if (this.listener != null) {
                this.listener.transferred(this.transferred);
            }
        }
    
        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            if (this.listener != null) {
                this.listener.transferred(this.transferred);
            }
        }
    }
}
