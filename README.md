# Swiss文档

## 一. 导入SDK

**Gradle 编译环境（Android Studio）**

​	(1)在 project 级别的 build.gradle 文件中添加 

```
allprojects {
    repositories {
    	//添加这一句
        maven{ url "https://dl.bintray.com/yom9c/maven" }
        jcenter()
        google()
    }
}
```

​	(2)在 module 级别的 build.gradle 文件中添加依赖

```
implementation 'com.sabinetek.swiss:SwissSdk:1.3.9'
```

## 二. 初始化SDK

在application的onCreate()方法中初始化

```java
@Override
public void onCreate() {
    super.onCreate();
    //初始化SDK
    Swiss.initialize(this);
}
```

## 三. 基本使用

​	(1).所以对设备的操作必须在耳机连接以后再执行，也就是STATE_CONNECTED以后

```Java
Swiss.getSwiss().setOnStateChangeListener(new BluetoothHelper.OnStateChangeListener() {
    @Override
    public void onStateChange(State state) {
        /*
        总共5个状态
        STATE_NONE(0),  //没有任何连接
    	STATE_LISTEN(1),//开始连接
    	STATE_CONNECTING(2),//连接中
    	STATE_CONNECTED(3),//已经连接
   		STATE_DISCONNECT(4);//断开连接
        */
    }
});	
```

​	(2).开始录音&停止录音

```java
Swiss.getSwiss().startRecord(new AudioDispatcher.OnAudioReceiver() {
    @Override
    public void onReceiveData(byte[] data, int len) {
        //data is pcm data
    }
});
```

​	(3).获取设备信息

```java
DevicesInfo.getInstance().setOnDeviceInfoListener(new DevicesInfo.OnDeviceInfoListener() {
    @Override
    public void onSuccess(DeviceType type) {
      if(type == DeviceType.VERSION_INFO){
      //协议版本
      DevicesInfo.getInstance().getProtocolVersion();
      //固件版本
      DevicesInfo.getInstance().getFirmwareVersion();
      //硬件版本
      DevicesInfo.getInstance().getHardwareVersion();
      //编码器版本
      DevicesInfo.getInstance().getCodecVersion();
      //制造商
      DevicesInfo.getInstance().getManufacture();
      //授权商
      DevicesInfo.getInstance().getDeviceLicensed();
      //设备名称
      DevicesInfo.getInstance().getProductionName();
    }else if(type == DeviceType.MEDIA_INFO){
      //采样率
      DevicesInfo.getInstance().getSampleRates();
      //声道
      DevicesInfo.getInstance(). getChannel();
    }
});
```

​	 (4).设置设备参数

```java
Swiss.getSwiss().setMicVolume() //设置麦克风音量 0-100
Swiss.getSwiss().setMonitorVolume() //设置监听音量(设备中听到自己的声音) 0-100
Swiss.getSwiss().setAgcEnable() //设置自动增益 true or false
Swiss.getSwiss().setAns() // 设置降噪 0-1-2-3 值越大，消除噪音越大 
Swiss.getSwiss().setReverberationRatio() //设置混响值0-100
Swiss.getSwiss().setMusicMixer() //设置混音大小0-100

```

更多请参看sample中内容