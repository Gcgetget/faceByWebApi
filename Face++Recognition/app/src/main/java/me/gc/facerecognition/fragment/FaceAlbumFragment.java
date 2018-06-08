package me.gc.facerecognition.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Decoder.BASE64Decoder;
import me.gc.facerecognition.R;
import me.gc.facerecognition.util.Constant;
import me.gc.facerecognition.util.HttpUtil;

/**
 * Created by Administrator on 2018/5/3 0003.
 */

public class FaceAlbumFragment extends Fragment {

    private Button update_album;
    private ListView listView;
    //定义一个列表集合
    List<Map<String,Object>> listItems;
    Map<String, Object> map;
    //定义一个simpleAdapter,供列表项使用
    SimpleAdapter simpleAdapter;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.facealbum, container, false);
        update_album = (Button)view.findViewById(R.id.update_album);
        initEvent();
        return view;
    }

    private void initEvent() {
        //创建人脸集合
        createFaceSet();

        //初始化列表集合
        getFaceDetail();

        update_album.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                getFaceDetail();
            }
        });
    }

    public JSONArray getFaceDetail(){

        final JSONObject json=new JSONObject();
        //后台接口
        String url=Constant.htUrl+"/android/getallface";
        HttpUtil.get(url,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                try {
                    //Log.d("de", "onSuccess: "+s);
                  JSONArray  jsonArray = new JSONArray(s);
                    json.put("result",jsonArray) ;
                    Log.d("ht",jsonArray.toString());
                    listItemsInit(jsonArray);  //将后台数据放入listView
                    loadAdapter();  //加载适配器到listView
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {

                super.onFailure(throwable, s);
                Log.d("de", "onFailure: "+s);
            }
        });
        try {
            return json.getJSONArray("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
         * 初始化适配器需要的数据格式
         */
    private void listItemsInit(JSONArray json) {
        listItems=new ArrayList<Map<String, Object>>();
        for (int i = 0; i < json.length(); i++){
            map = new HashMap<String, Object>();
            try {
                JSONObject jsonob=json.getJSONObject(i);
                map.put("image",jsonob.getString("image"));
                map.put("name",jsonob.getString("username"));
                map.put("sex",jsonob.getString("gender"));
                map.put("age",jsonob.getString("age"));
                map.put("yz",jsonob.getString("beauty"));
                //把列表项加进列表集合
                listItems.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }

    }

    private void loadAdapter() {

        listView = (ListView) view.findViewById(R.id.list_view);
        // key值数组，适配器通过key值取value，与列表项组件一一对应
        String[] from=new String[]{"image","name","sex","age","yz"};
        // 列表项组件Id 数组
        int[] to=new int[]{R.id.iv_face, R.id.tv_name_v, R.id.tv_sex_v,
                R.id.tv_age_v, R.id.tv_yz_v};
        simpleAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.face_item,
                from, to);

        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object o, String s) {
                if((view instanceof ImageView) && (o instanceof String)) {
                    ImageView vi = (ImageView) view;
                    String url = (String) o;
                    try {
                        byte[] bytes = new BASE64Decoder().decodeBuffer(url);
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        vi.setImageBitmap(bm);

                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        listView.setAdapter(simpleAdapter);
    }

    private void createFaceSet() {
        RequestParams params = new RequestParams();
        params.put("api_key", Constant.Key);
        params.put("api_secret",Constant.Secret);
        params.put("display_name","FaceSetCheng");  //人脸集合的名字
        params.put("outer_id",Constant.outer_id);  //人脸集合的标识

        //调用API发起网络请求
        HttpUtil.post(Constant.createUrl,params,new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String resultJson) {
                super.onSuccess(resultJson);
                try {
                    JSONObject jsonObject=new JSONObject(resultJson);
                    Log.d("de",resultJson.toString());
                    Constant.facesetToken=jsonObject.getString("faceset_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("createFaceSet==Success","success");
                Log.d("resultJson===",resultJson.toString());
            }

            @Override
            public void onFailure(Throwable throwable, String resultJson) {
                super.onFailure(throwable, resultJson);
                Log.d("createFaceSet==Error","error");
                Log.d("resultJson===",resultJson.toString());
               // Toast.makeText(getActivity(),"createFaceSet=="+resultJson.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }

}