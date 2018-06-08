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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import Decoder.BASE64Decoder;
import me.gc.facerecognition.R;
import me.gc.facerecognition.util.Constant;
import me.gc.facerecognition.util.HttpUtil;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2018/5/3 0003.
 */

public class FaceSearchFragment extends Fragment implements View.OnClickListener {
    private Uri imageUri;
    private String similarity;
    private ImageView iv_face;
    private Button b_face_choice;
    private Button b_face_enter;
    private TextView tv_similar;
    private ImageView iv_result_face;
    private TextView tv_name_v;
    private TextView tv_sex_v;
    private TextView tv_age_v;
    private TextView tv_yz_v;
    private static final int SCALE = 5;//照片缩小比例
    private static final int TAKE_PICTURE = 0;
    private static final int CHOOSE_PICTURE = 1;
    private View view;
    private View search_face;
    private View no_face;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.facesearch, container, false);
        initViews();
        initEvent();
        return view;
    }

    private void initViews() {
        iv_face = (ImageView) view.findViewById(R.id.iv_face);
        b_face_choice = (Button) view.findViewById(R.id.b_face_choice);
        b_face_enter = (Button) view.findViewById(R.id.b_face_enter);

        search_face = (View)view.findViewById(R.id.search_face);
        no_face = (View) view.findViewById(R.id.no_face);

        tv_similar = (TextView)view.findViewById(R.id.tv_search_similar);
        iv_result_face = (ImageView)view.findViewById(R.id.iv_search_face);
        tv_name_v = (TextView)view.findViewById(R.id.tv_search_name_v);
        tv_sex_v = (TextView)view.findViewById(R.id.tv_search_sex_v);
        tv_age_v = (TextView)view.findViewById(R.id.tv_search_age_v);
        tv_yz_v = (TextView)view.findViewById(R.id.tv_search_yz_v);
    }

    private void initEvent() {
        b_face_choice.setOnClickListener(this);
        b_face_enter.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_face_choice:
                choosePicture();
                break;
            case R.id.b_face_enter:
                searchFace();
                break;
            default:
                break;
        }
    }

    private void searchFace() {
        //将图片转化为bitmap
        Bitmap bitmap=((BitmapDrawable)iv_face.getDrawable()).getBitmap();
        Log.d("de",bitmap.toString());
        //这里api要求传入一个字节数组数据，因此要用字节数组输出流
        ByteArrayOutputStream oStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,oStream);
        byte[] image=oStream.toByteArray();
        String image_base64 = Constant.encode(image);

        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("image_base64",image_base64);  //待搜索的人脸照片
        params.put("outer_id",Constant.outer_id);  //所要搜索的人脸集合标识

        //调用search API发起网络请求
        HttpUtil.post(Constant.searchUrl,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                //Toast.makeText(getActivity(),"调用成功",Toast.LENGTH_SHORT).show();
                Log.d("de","success");
                Log.d("de",resultJson.toString());

                try {
                    JSONObject object = new JSONObject(resultJson);
                    JSONArray array = object.getJSONArray("results");
                    //比对结果置信度
                    //similarity=object.getString("confidence");
                    similarity=array.getJSONObject(0).getString("confidence");

                    //用于参考的置信度阈值1e-3,1e-4,1e-5
                    //如果置信值低于“千分之一”阈值则不建议认为是同一个人；
                    // 如果置信值超过“十万分之一”阈值，则是同一个人的几率非常高。
                    JSONObject thresholds = object.getJSONObject("thresholds");

                    //1e-3
                    String yz_3=thresholds.getString("1e-3");
                    Log.d("de",yz_3);
                    if(Double.parseDouble(similarity)>Double.parseDouble(yz_3)){
                        getditailByFaceToken(array);
                        search_face.setVisibility(View.VISIBLE);
                        no_face.setVisibility(View.GONE);

                    }else{
                        no_face.setVisibility(View.VISIBLE);
                        search_face.setVisibility(View.GONE);
                    }
                    //JSONArray array = object.getJSONArray("results");
                    //getditailByFaceToken(array);
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
//        Toast.makeText(getActivity(),"上传图片无法获取人脸标识",Toast.LENGTH_SHORT).show();
    }


    private void getditailByFaceToken(final JSONArray jsonArray){
        String face_token="";

        for (int i = 0; i <jsonArray.length() ; i++) {
            try {
                face_token += jsonArray.getJSONObject(i).getString("face_token")+";";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        RequestParams params = new RequestParams();
        params.put("face_token", face_token);  //人脸face_token
        String url=Constant.htUrl+"/android/getbyfacetoken";
        HttpUtil.post(url,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    JSONArray js=new JSONArray(s);
                    JSONObject jo=js.getJSONObject(0);
                    String image=jo.getString("image");
                    byte[]bytes=new BASE64Decoder().decodeBuffer(image);
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    iv_result_face.setImageBitmap(bm);
                    tv_name_v.setText(jo.getString("username"));
                    tv_sex_v.setText(jo.getString("gender"));
                    tv_age_v.setText(jo.getString("age"));
                    tv_yz_v.setText(jo.getString("beauty"));

                    String sim=jsonArray.getJSONObject(0).getString("confidence");
                    tv_similar.setText(sim);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                super.onFailure(throwable, s);
            }
        });


    }



    private void choosePicture() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("图片来源");
        dialog.setNegativeButton("取消", null);
        dialog.setItems(new String[]{"拍照", "相册"}, new DialogInterface.OnClickListener() {

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
                File outputImage=new File(getActivity().getExternalCacheDir(),"output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(Build.VERSION.SDK_INT>=24){
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
//        Fragment selFragment = new Fragment();
//        selFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PICTURE:
                    try {
                    //将拍摄的照片显示出来
                    Bitmap bitmap= BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageUri));
                    iv_face.setImageBitmap(bitmap);
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
                            //iv_face.setImageBitmap(photo);
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
