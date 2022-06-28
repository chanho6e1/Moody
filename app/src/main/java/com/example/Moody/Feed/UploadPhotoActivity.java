package com.example.Moody.Feed;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Activity.MainActivity;
import com.example.Moody.R;
import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class UploadPhotoActivity  extends BaseActivity {
    String emotion="null";
    String resultS;
    Uri selectedImageUri;
    ImageView image;
    TextView tag_field, resultTV;

    //UCROP
    private static final int REQUEST_SELECT_PICTURE = 0x01;
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage.jpeg";

    private static final int RATIO_ORIGIN = 0;
    private static final int RATIO_SQUARE = 1;
    private static final int RATIO_DYNAMIC = 2;
    private static final int RATIO_CUSTOM = 3;

    private static final int FORMAT_PNG = 0;
    private static final int FORMAT_WEBP = 1;
    private static final int FORMAT_JPEG = 2;

    private Uri mDestinationUri;
    //private ImageView mImageView;
    private Uri mResultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //아이디별 db파일 호출

        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_photo_activity);

        //뒤로가기 버튼
        ImageView back_btn = (ImageView) findViewById(R.id.upload_back_btn);
        LinearLayout sel_btn = (LinearLayout) findViewById(R.id.upload_sel_btn);
        LinearLayout upload_btn = (LinearLayout) findViewById(R.id.ok_Layout);

        image = (ImageView) findViewById(R.id.upload_image);
        tag_field = (TextView) findViewById(R.id.tag_field);
        resultTV = (TextView) findViewById(R.id.resultTV);

        mDestinationUri = Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME));

        back_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadPhotoActivity.this, MainActivity.class);
                intent.putExtra("fragment","feed");
                startActivity(intent);
                finish();
            }
        });

        //UCROP
        sel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });

        //이미지 등록
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[]image=getByteArray();
                    LoginActivity.dbHelper.insert(image, emotion, resultS);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(),"이미지가 등록되었습니다", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(UploadPhotoActivity.this, MainActivity.class);
                intent.putExtra("fragment","feed");
                startActivity(intent);
                LoginActivity.dbHelper.close();
            }
        });
    }

    //UCROP
    //go to gallery and pick the image
    private void pickFromGallery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN // Permission was added in API Level 16
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);    // @see onRequestPermissionsResult()
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.label_select_picture)),
                    REQUEST_SELECT_PICTURE);
        }
    }

    //start crop
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PICTURE) {
                selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    startCropActivity(data.getData());
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    //After image crop
    private void handleCropResult(@NonNull Intent result) {
        mResultUri = UCrop.getOutput(result);
        if (mResultUri != null) {
            Uri cropImage = mResultUri;
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.setImageBitmap(bitmap);
            try {
                emotion = getEmotion(cropImage);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //crop error
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e("crop", "handleCropError: ", cropError);
            Toast.makeText(UploadPhotoActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startCropActivity(@NonNull Uri uri) {
        UCrop uCrop = UCrop.of(uri, mDestinationUri);

        uCrop = _setRatio(uCrop, RATIO_ORIGIN, 0, 0);
        uCrop = _setSize(uCrop, 0, 0);

        uCrop = _advancedConfig(uCrop, FORMAT_JPEG, 90);

        uCrop.start(UploadPhotoActivity.this);
    }

    private UCrop _setRatio(@NonNull UCrop uCrop, int choice, float xratio, float yratio){
        switch (choice) {
            case RATIO_ORIGIN:
                uCrop = uCrop.useSourceImageAspectRatio();
                break;
            case RATIO_SQUARE:
                uCrop = uCrop.withAspectRatio(1, 1);
                break;
            case RATIO_DYNAMIC:
                // do nothing
                break;
            case RATIO_CUSTOM:
            default:
                try {
                    float ratioX = xratio;
                    float ratioY = yratio;
                    if (ratioX > 0 && ratioY > 0) {
                        uCrop = uCrop.withAspectRatio(ratioX, ratioY);
                    }
                } catch (NumberFormatException e) {
                    Log.e("Crop", "Number please", e);
                }
                break;
        }

        return uCrop;

    }

    private UCrop _setSize(@NonNull UCrop uCrop, int maxWidth, int maxHeight){
        if(maxWidth > 0 && maxHeight > 0){
            return uCrop.withMaxResultSize(maxWidth, maxHeight);
        }
        return uCrop;
    }

    private UCrop _advancedConfig(@NonNull UCrop uCrop, int format, int quality) {
        UCrop.Options options = new UCrop.Options();


        switch (format) {
            case FORMAT_PNG:
                options.setCompressionFormat(Bitmap.CompressFormat.PNG);
                break;
            case FORMAT_WEBP:
                options.setCompressionFormat(Bitmap.CompressFormat.WEBP);
                break;
            case FORMAT_JPEG:
            default:
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                break;
        }
        options.setCompressionQuality(quality); // range [0-100]

        return uCrop.withOptions(options);
    }

    //이미지 바이트 단위로 변환
    public byte[]getByteArray() throws IOException {
        Bitmap byteBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        byteBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[]data= stream.toByteArray();
        return data;
    }

    public String getEmotion(Uri cropImage) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cropImage);
        int w= bitmap.getWidth();
        int h= bitmap.getHeight();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) 48, (int) 48, true);

        float[][][][] bytes_img = new float[1][48][48][1];

        int k = 0;
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 48; y++) {
                int pixel = resized.getPixel(x, y);      // ARGB : ff4e2a2a

                bytes_img[0][y][x][0] = (Color.red(pixel)) / (float) 255;
            }
        }
        Interpreter tf_lite = getTfliteInterpreter("acc65.tflite");

        float[][] output = new float[1][7];
        tf_lite.run(bytes_img, output);

        String[] emotion = {"Angry", "Disgust", "Fear","Happy","Sad","Surprise", "Natural"};

        int maxIdx = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < 6; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }

        int[] result_array = new int[6];
        for(int i=0; i<7; i++) {
            int percent = (int) Math.round(output[0][i] * 100);
            if(i != 6)
                result_array[i] = percent;
            else
                result_array[maxIdx] += percent;
        }

        resultS ="";
        for(int i=0; i<6; i++) {
            if(result_array[i] != 0) {
                resultS += emotion[i] + " " + result_array[i] + "% ";
            }
        }


        tag_field.setText("#"+emotion[maxIdx]);
        resultTV.setText(resultS);
        return emotion[maxIdx];
    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(UploadPhotoActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //모델을 읽어오는 함수
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}

