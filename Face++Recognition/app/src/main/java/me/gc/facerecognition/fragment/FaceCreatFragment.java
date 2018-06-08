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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
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

public class FaceCreatFragment extends Fragment implements View.OnClickListener {


    private static final int TAKE_PICTURE = 0;
    private static final int CHOOSE_PICTURE = 1;
    private ImageView iv_face;
    private TextView result;
    private Button b_face_add;
    private Button b_face_choice;
    private EditText e_name;
    private Uri imageUri;

    private View view;
    private static final int SCALE = 5;//照片缩小比例

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view =inflater.inflate(R.layout.facecreate,container,false);
        initViews();
        initEvent();
        getDetail();
        return view;
    }

    private void initViews() {
        iv_face=(ImageView)view.findViewById(R.id.iv_face);
        b_face_choice=(Button)view.findViewById(R.id.b_face_choice);
        b_face_add=(Button)view.findViewById(R.id.b_face_enter);
        e_name=(EditText)view.findViewById(R.id.e_face_name);
        //result=(TextView)view.findViewById(R.id.result);
    }

    private void initEvent() {
        b_face_choice.setOnClickListener(this);
        b_face_add.setOnClickListener(this);
    }

    @Override

    public void onClick(View view) {

        switch (view.getId()){

            case R.id.b_face_choice:
                choosePicture();
                break;
            case R.id.b_face_enter:
                detectFace();
                break;
        }
    }


    private void detectFace() {

        //将图片转化为bitmap
        final Bitmap bitmap=((BitmapDrawable)iv_face.getDrawable()).getBitmap();
        Log.d("de",bitmap.toString());
        //这里api要求传入一个字节数组数据，因此要用字节数组输出流
        ByteArrayOutputStream oStream=new ByteArrayOutputStream();
                /*Bitmap.compress()方法可以用于将Bitmap-->byte[]
                      既将位图的压缩到指定的OutputStream。如果返回true，
                      位图可以通过传递一个相应的InputStream BitmapFactory.decodeStream（重建）
                      第一个参数可设置JPEG或PNG格式,第二个参数是图片质量，第三个参数是一个流信息*/
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,oStream);

        final byte[] image=oStream.toByteArray();

        final String image_base64 = Constant.encode(image);

        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("image_base64",image_base64);  //人脸照片对应的编码格式
        params.put("return_attributes","gender,age,beauty");  //设置需要返回的人脸信息

        //调用detect API发起网络请求
        HttpUtil.post(Constant.detectUrl,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                //Toast.makeText(getActivity(),"调用成功",Toast.LENGTH_SHORT).show();
                //Log.d("de","success");
                //Log.d("de",resultJson.toString());
                //result.setText(resultJson.toString());
                if(resultJson != null){
                    JSONObject object;
                    try {
                        object = new JSONObject(resultJson);
                        JSONArray array = object.getJSONArray("faces");
                        String face_token = null;

                        for(int i = 0;i<array.length();i++){
                            JSONObject oj = (JSONObject) array.get(i);
                            face_token = (String) oj.get("face_token");
                            if(face_token != null){
                                Log.d("de",face_token);
//                                    addFace(face_token);
                                //将face_token发送到人脸集合
                                sendFaceToken(face_token);
                                //将人脸信息发送到数据库保存
                                sendFaceInfo(oj,image_base64);

                              // getDetail();
                            }else{
                                Toast.makeText(getActivity(),"上传图片无法获取人脸标识",Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Throwable throwable, String resultJson) {

                Toast.makeText(getActivity(),"调用失败",Toast.LENGTH_SHORT).show();
                Log.d("de","error");
                Log.d("de",resultJson.toString());
                Toast.makeText(getActivity(),"detect=="+resultJson.toString(),Toast.LENGTH_SHORT).show();
                result.setText(resultJson.toString());

                super.onFailure(throwable, resultJson);
            }
        });
//        Toast.makeText(getActivity(),"上传图片无法获取人脸标识",Toast.LENGTH_SHORT).show();
    }

    private void sendFaceInfo(JSONObject jsonObject,String image){
        RequestParams params = new RequestParams();
        try {
            //检测出的人脸信息
            JSONObject attr=jsonObject.getJSONObject("attributes");
            String gender=attr.getJSONObject("gender").getString("value");
            //人脸照片、名字、性别、年龄、颜值、标识
            params.put("image",image);
            params.put("username",e_name.getText().toString());
            params.put("gender",attr.getJSONObject("gender").getString("value"));
            params.put("age",attr.getJSONObject("age").getString("value"));
            if(gender=="Male"){
                params.put("beauty",attr.getJSONObject("beauty").getString("male_score"));
            }else{
                params.put("beauty",attr.getJSONObject("beauty").getString("female_score"));
            }
            //params.put("beauty",attr.getJSONObject("beauty").getString("female_score")+";"+attr.getJSONObject("beauty").getString("male_score"));
            params.put("facetoken",jsonObject.getString("face_token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //后台接口
        String url=Constant.htUrl+"/android/addfaceinfo";
        //发起网络请求发送人脸数据
        HttpUtil.post(url,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                Log.d("de",resultJson.toString());
                Toast.makeText(getActivity(),"添加人脸成功",Toast.LENGTH_SHORT).show();

                //result.setText(resultJson.toString());
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.d("de",s.toString());
            }
        });

    }

    private void getDetail(){
        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("outer_id",Constant.outer_id);
        String url="https://api-cn.faceplusplus.com/facepp/v3/faceset/getdetail";
        HttpUtil.post(url,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                Log.d("def",resultJson.toString());
//                result.setText(resultJson.toString());
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
                Log.d("de",s.toString());
            }
        });
    }

    private void sendFaceToken(String facetoken){
        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("face_tokens",facetoken);   //人脸照片标识
        params.put("outer_id",Constant.outer_id);  //人脸集合标识

        //调用addFace API发起网络请求，将人脸加入到集合中
        HttpUtil.post(Constant.addfaceUrl,params,new AsyncHttpResponseHandler() {
            @Override
        public void onSuccess(String resultJson) {
            super.onSuccess(resultJson);
            Log.d("de","success");
            Log.d("de",resultJson.toString());
//            result.setText(resultJson.toString());
                try {
                    JSONObject result=new JSONObject(resultJson);
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
            //result.setText(resultJson.toString());

            super.onFailure(throwable, resultJson);
        }
    });

    }


    private void choosePicture() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("图片来源");
        dialog.setNegativeButton("取消", null);
        dialog.setItems(new String[]{"拍照","相册"}, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case TAKE_PICTURE:
                        takePhoto();
                        break;
                    case CHOOSE_PICTURE:
                        chooseAlbum();
                        break;
                    default:
                        break;
                }
            }
            private void chooseAlbum() {

                Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setType("image/*");
                startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
            }

            private void takePhoto() {
//                Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                Uri imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"image.jpg"));
//                //指定照片保存路径（SD卡），image.jpg为一个临时文件，每次拍照后这个图片都会被替换
//                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(openCameraIntent, TAKE_PICTURE);
//
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
                startActivityForResult(intent, TAKE_PICTURE);
            }
        });
        dialog.create().show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
//                    //将保存在本地的图片取出并缩小后显示在界面上
//                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/image.jpg");
//                    Bitmap newBitmap = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
//                    //由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
//                    bitmap.recycle();
//
//                    //将处理过的图片显示在界面上，并保存到本地
//                    iv_face.setImageBitmap(newBitmap);
//                    ImageTools.savePhotoToSDCard(newBitmap, Environment.getExternalStorageDirectory().getAbsolutePath(), String.valueOf(System.currentTimeMillis()));
                    try {
                        //将拍摄的照片显示出来
                        Bitmap bitmap= BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                        Bitmap newBitmap = ImageTools.zoomBitmap(bitmap, bitmap.getWidth() / SCALE, bitmap.getHeight() / SCALE);
                       //由于Bitmap内存占用较大，这里需要回收内存，否则会报out of memory异常
                        bitmap.recycle();

                        //将处理过的图片显示在界面上，并保存到本地
                        iv_face.setImageBitmap(newBitmap);
                        //ImageTools.savePhotoToSDCard(newBitmap, Environment.getExternalStorageDirectory().getAbsolutePath(), String.valueOf(System.currentTimeMillis()));
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }

                    break;

                case CHOOSE_PICTURE:
                    ContentResolver resolver = getActivity().getContentResolver();
                    //照片的原始资源地址
                    Uri originalUri = data.getData();
                    try {
                        //使用ContentProvider通过URI获取原始图片
                        Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                        if (photo != null) {
                            //为防止原始图片过大导致内存溢出，这里先缩小原图显示，然后释放原始Bitmap占用的内存
                            Bitmap smallBitmap = ImageTools.zoomBitmap(photo, photo.getWidth() / SCALE, photo.getHeight() / SCALE);
                            //释放原始图片占用的内存，防止out of memory异常发生
                            photo.recycle();

                            iv_face.setImageBitmap(smallBitmap);
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
