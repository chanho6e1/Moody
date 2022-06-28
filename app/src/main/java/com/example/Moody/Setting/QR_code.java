package com.example.Moody.Setting;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Moody.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class QR_code extends AppCompatActivity {
    private Bitmap mQRBmp;
    private ImageView ivQrcode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        ivQrcode =  findViewById(R.id.iv_qr_code);
        findViewById(R.id.QRcode_backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String email = getIntent().getStringExtra("email");
        doQRCode(email);
    }

    private void doQRCode(String qrcode) {
        int bmpWidth = dip2px(this, 250);
        if (mQRBmp != null && !mQRBmp.isRecycled()) {
            mQRBmp.recycle();
            mQRBmp = null;
        }

        mQRBmp = createQRImage(qrcode, bmpWidth,
                bmpWidth);
        ivQrcode.setImageBitmap(mQRBmp);


    }
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    public static Bitmap createQRImage(String url, final int width, final int height) {
        int marginWhite = 0;
        try {
            // url이 맞는지 판단
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 매트릭스 변환을 사용하여 이미지 데이터 변환
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            boolean isFirstBlackPoint = false;
            int startX = 0;
            int startY = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        if (isFirstBlackPoint == false){
                            isFirstBlackPoint = true;
                            startX = x;
                            startY = y;
                        }
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            if (startX <= marginWhite) return bitmap;

            int x1 = startX - marginWhite;
            int y1 = startY - marginWhite;
            if (x1 < 0 || y1 < 0) return bitmap;

            int w1 = width - x1 * 2;
            int h1 = height - y1 * 2;

            Bitmap bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);
            return bitmapQR;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
