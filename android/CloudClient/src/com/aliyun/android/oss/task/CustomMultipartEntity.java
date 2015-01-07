package com.aliyun.android.oss.task;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import com.cloud.client.file.MissionListener;

public class CustomMultipartEntity extends MultipartEntity {

    private MissionListener listener;
    public CountingOutputStream cos;
    
    public CustomMultipartEntity() {
        super();
    }
    
    public CustomMultipartEntity(final MissionListener listener) {
        super();
        this.listener = listener;
    }

    public CustomMultipartEntity(final HttpMultipartMode mode,
            final MissionListener listener) {
        super(mode);
        this.listener = listener;
    }

    public CustomMultipartEntity(HttpMultipartMode mode, final String boundary,
            final Charset charset, final MissionListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    public void setProgressListener(MissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        cos = new CountingOutputStream(outstream, this.listener);
        super.writeTo(cos);
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
            // TODO Auto-generated method stub
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