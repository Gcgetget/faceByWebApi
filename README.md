# faceByWebApi
使用face++在线的api 进行人脸信息的处理，包含建立人脸库，人脸搜索，人脸比对。

1.注册人脸，将人脸添加到人脸库 
- 调用face++ CreateFaceSetAPI创建人脸集合; 
- 调用face++ Detect API检测人脸； 
- 将检测出的人脸face_token加入到人脸集合中;  
- 把返回的人脸信息保存到数据库从而创建人脸库。 
测试结果如下:</br> 
新建人脸 
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/xjrl.png) </br>
展示人脸库中人脸信息  
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/rlk.png)
</br>
2.人脸搜索。调用face++Search API从人脸集合中（人脸库）搜索人脸，人脸库中返回人脸信息</br>
人脸搜索测试如下：</br>
人脸搜索1 
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/rlss1.png)
人脸搜索2 
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/rlss2.png)
3. 调用face++Compare API进行人脸比对，根据置信度值判断是否是同一个人。</br>
比对测试如下：<br>
人脸比对1 
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/rlbd1.png)
人脸比对2 
![](https://github.com/Gcgetget/faceByWebApi/blob/master/test/rlbd2.png)
