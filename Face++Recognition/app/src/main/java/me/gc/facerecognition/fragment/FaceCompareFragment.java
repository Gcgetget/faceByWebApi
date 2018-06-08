package me.gc.facerecognition.fragment;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import me.gc.facerecognition.R;
import me.gc.facerecognition.util.Constant;
import me.gc.facerecognition.util.HttpUtil;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2018/5/3 0003.
 */

public class FaceCompareFragment extends Fragment implements View.OnClickListener {
    private String similarity;  //相似度
    private ImageView iv_img1;
    private ImageView iv_img2;
    private Button btn_choose_img1;
    private Button btn_choose_img2;
    private TextView tv_similar;
    private TextView tv_isonperson;
    private Button btn_submit;
    private Uri imageUri;  //拍照的照片
    private View view;
    private Bitmap bitmap1 ;
    private Bitmap bitmap2 ;
    private static final int PICTURE_1=1;  //第一张照片标识
    private static final int PICTURE_2=2;  //第二张照片标识
    private static final int TAKE_PHOTO= 0;  //拍照
    private static final int CHOOSE_PHOTO= 1;  //相册
    private static final int TAKE_PHOTO_1 = 0;  //第一张照片拍照
    private static final int TAKE_PHOTO_2 = 1;  //第二张照片拍照
    private static final int CHOOSE_PHOTO_1 = 2;  //选择第一张照片
    private static final int CHOOSE_PHOTO_2 = 3;  //选择第二张照片
    private static final int SCALE = 5;//照片缩小比例
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view =inflater.inflate(R.layout.facecompare,container,false);
        initViews();
        initEvent();
        return view;
    }

    private void initViews() {
        iv_img1 = (ImageView) view.findViewById(R.id.iv_img1);
        iv_img2 = (ImageView) view.findViewById(R.id.iv_img2);

        btn_choose_img1 = (Button)view. findViewById(R.id.btn_choose_img1);
        btn_choose_img2 = (Button)view. findViewById(R.id.btn_choose_img2);
        btn_submit = (Button)view. findViewById(R.id.btn_submit);

        tv_similar = (TextView)view. findViewById(R.id.tv_similar);
        tv_isonperson = (TextView) view.findViewById(R.id.tv_isonperson);


    }

    private void initEvent(){
        btn_choose_img1.setOnClickListener(this);
        btn_choose_img2.setOnClickListener(this);
        btn_submit.setOnClickListener(this);
    }
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_choose_img1:
                choosePicture(PICTURE_1);
                break;
            case R.id.btn_choose_img2:
                choosePicture(PICTURE_2);
                break;
            case R.id.btn_submit:
                compareFace();
                break;
        }
    }

    private void compareFace() {
        bitmap1=((BitmapDrawable)iv_img1.getDrawable()).getBitmap();
        bitmap2=((BitmapDrawable)iv_img2.getDrawable()).getBitmap();
        ByteArrayOutputStream oStream1=new ByteArrayOutputStream();
                /*Bitmap.compress()方法可以用于将Bitmap-->byte[]
                      既将位图的压缩到指定的OutputStream。如果返回true，
                      位图可以通过传递一个相应的InputStream BitmapFactory.decodeStream（重建）
                      第一个参数可设置JPEG或PNG格式,第二个参数是图片质量，第三个参数是一个流信息*/
        bitmap1.compress(Bitmap.CompressFormat.JPEG,100,oStream1);

        final byte[] image1=oStream1.toByteArray();
        final String image_base64_1 = Constant.encode(image1);

        ByteArrayOutputStream oStream2=new ByteArrayOutputStream();
                /*Bitmap.compress()方法可以用于将Bitmap-->byte[]
                      既将位图的压缩到指定的OutputStream。如果返回true，
                      位图可以通过传递一个相应的InputStream BitmapFactory.decodeStream（重建）
                      第一个参数可设置JPEG或PNG格式,第二个参数是图片质量，第三个参数是一个流信息*/
        bitmap2.compress(Bitmap.CompressFormat.JPEG,100,oStream2);

        final byte[] image2=oStream2.toByteArray();
        final String image_base64_2= Constant.encode(image2);

        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("image_base64_1",image_base64_1);  //第一张照片
        params.put("image_base64_2",image_base64_2);  //第二张照片

        //调用compare API发起网络请求
        HttpUtil.post(Constant.compareUrl,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                //Toast.makeText(getActivity(),"调用成功",Toast.LENGTH_SHORT).show();
                Log.d("de","success");
                Log.d("de",resultJson.toString());
                try {
                    JSONObject jsonObject=new JSONObject(resultJson);
                    //比对结果置信度
                    similarity=jsonObject.getString("confidence");

                    //用于参考的置信度阈值1e-3,1e-4,1e-5
                    //如果置信值低于“千分之一”阈值则不建议认为是同一个人；
                    // 如果置信值超过“十万分之一”阈值，则是同一个人的几率非常高。
                    JSONObject thresholds = jsonObject.getJSONObject("thresholds");

                    //1e-3
                    String yz_3=thresholds.getString("1e-3");
                    Log.d("de",yz_3);
                    if(Double.parseDouble(similarity)>Double.parseDouble(yz_3)){
                        tv_isonperson.setText("可能性很高");
                    }else{
                        tv_isonperson.setText("可能性很低");
                    }
                    tv_similar.setText(similarity);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(Throwable throwable, String resultJson) {

                Toast.makeText(getActivity(),"调用失败",Toast.LENGTH_SHORT).show();
                Log.d("de","error");
                Log.d("de",resultJson.toString());
                Toast.makeText(getActivity(),"detect=="+resultJson.toString(),Toast.LENGTH_SHORT).show();


                super.onFailure(throwable, resultJson);
            }
        });

    }

    private void choosePicture(final int choosePhoto) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("图片来源");
        dialog.setNegativeButton("取消", null);
        dialog.setItems(new String[]{"拍照","相册"}, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case TAKE_PHOTO:
                        if(choosePhoto==PICTURE_1) {
                            takePhoto(TAKE_PHOTO_1);
                        }else {
                            takePhoto(TAKE_PHOTO_2);
                        }
                        break;
                    case CHOOSE_PHOTO:
                        if(choosePhoto==PICTURE_1){
                            chooseAlbum(CHOOSE_PHOTO_1);
                        }else{
                            chooseAlbum(CHOOSE_PHOTO_2);
                        }
                        break;
                    default:
                        break;
                }
            }
            private void chooseAlbum(int choosePhoto) {
                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setType("image/*");
                startActivityForResult(openAlbumIntent, choosePhoto);
            }

            private void takePhoto(int choosePhoto) {
                File outputImage=new File(getActivity().getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }if(Build.VERSION.SDK_INT>=24){
                    imageUri= FileProvider.getUriForFile(getActivity(),"com.example.cameraablumtest.fileprovider",outputImage);
                }else{
                    imageUri=Uri.fromFile(outputImage);
                }
                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, choosePhoto);
            }
        });
        dialog.create().show();
    }

    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO_1:
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap1= BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        Bitmap newBitmap1 = ImageTools.zoomBitmap(bitmap1, bitmap1.getWidth() / SCALE, bitmap1.getHeight() / SCALE);
                        //由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                        bitmap1.recycle();
                        //将处理过的图片显示在界面上，并保存到本地
                        iv_img1.setImageBitmap(newBitmap1);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    break;

                case TAKE_PHOTO_2:
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap2= BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        Bitmap newBitmap2 = ImageTools.zoomBitmap(bitmap2, bitmap2.getWidth() / SCALE, bitmap2.getHeight() / SCALE);
                        //由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                        bitmap2.recycle();
                        //将处理过的图片显示在界面上，并保存到本地
                        iv_img2.setImageBitmap(newBitmap2);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    break;

                case CHOOSE_PHOTO_1:
                    ContentResolver resolver1 = getActivity().getContentResolver();
                    //照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {
                        //使用ContentProvider通过URI获取原始图片
                        Bitmap photo1 = MediaStore.Images.Media.getBitmap(resolver1, originalUri);
                        if (photo1 != null) {
                            //为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            Bitmap smallBitmap1 = ImageTools.zoomBitmap(photo1, photo1.getWidth() / SCALE, photo1.getHeight() / SCALE);
                            //释放原始图片占用的内存，防止out of memory异常发生
                            photo1.recycle();

                            iv_img1.setImageBitmap(smallBitmap1);
                           // iv_img1.setImageBitmap(photo1);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case CHOOSE_PHOTO_2:
                    ContentResolver resolver2 = getActivity().getContentResolver();
                    //照片的原始资源地址 与第一张照片区别开
                    Uri originalUri1= data.getData();
                    try {
                        //使用ContentProvider通过URI获取原始图片
                        Bitmap photo2 = MediaStore.Images.Media.getBitmap(resolver2, originalUri1);
                        if (photo2 != null) {
                            Bitmap smallBitmap2 = ImageTools.zoomBitmap(photo2,photo2.getWidth()/ SCALE,photo2.getHeight()/ SCALE);
                            iv_img2.setImageBitmap(smallBitmap2);
                            //iv_img2.setImageBitmap(photo);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }




}
