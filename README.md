# SimpleAudioRecorder

一个简单的MD风格录音工具（仍在开发中…）

## 功能
* MD设计风格
* 录音过程中在UI上通过波形展示录音音量
* 录音过程中如果3秒内不说话那么自动结束录音
* 录音结果保存wav和flac两份
* 录音结果可以直接点击播放按钮进行播放

## demo
* 下载 [APK](https://github.com/ayaseruri/SimpleAudioRecorder/blob/master/demo/app-debug.apk?raw=true)
* 截图(两张)(图片较大,请耐心等待)
<br />

![Screenshot1](https://github.com/ayaseruri/SimpleAudioRecorder/blob/master/demo/Screenshot1.png?raw=true)
<br />

![Screenshot2](https://github.com/ayaseruri/SimpleAudioRecorder/blob/master/demo/Screenshot2.png?raw=true)
* gif展示(一张)(图片较大,请耐心等待):
<br />

![gif](https://github.com/ayaseruri/SimpleAudioRecorder/blob/master/demo/demo.gif?raw=true)

## 实现思路
* ### 波形绘制WaveView
  * WaveView内部维护一组点(Point)数据Points
  * Timer周期性执行以下操作:
    * 采样录音声音大小生成Y坐标
    * 依据时间间隔生成X坐标
    * 依据X,Y坐标生成新的点Point
    * 依据时间间隔将Points中位于末尾的Point移除并将新的Point加入
    * 发送UpdateUI msg给UIHandler
  * UIHandler 调用 invalidate()方法出发WaveView重新绘制
  * 重新绘制时WaveView自动调用onDraw()方法通过Points设置Path数据最终通过canvas.drawPath()绘制波形;

* ### 自动停止录音AutoStopper
  * 启用后台线程每N(例如20)毫秒时间内监测一次录音数据,如果录音数据大于阈值(ThreadHold)那么重置M秒等待时间,否则
  ```java
  M = M - N;
  ```
  如果M小于等于0，那么断定用户M秒内没有说话，录音结束

* ### Wav和Flac
  * 使用AudioRecord保存原始PCM录音信息写入文件头得到WAV格式文件
  * 使用第三方开源库 [AndroidAudioConverter](https://github.com/adrielcafe/AndroidAudioConverter)(库巨大，足足有9MB)将Wav转化为Flac格式，为了使转化过程对用户更加友好，使用了第三方开源库[FabProgressCircle](https://github.com/JorgeCastilloPrz/FABProgressCircle)进行了提示过度,用户在长时间录音之后需要等待一定时间进行转码
  * 其实更加理想的方式是在录音的过程中进行Flac文件的转码和保存,具体思路可以参考[Lame](http://lame.sourceforge.net/)以及相关资料[Android MP3录音实现 ](http://www.cnblogs.com/ct2011/p/4080193.html)可是没有找到Flac格式的，记得之前使用[科大讯飞](http://www.iflytek.com/)语音识别库的时候他们的录音可以直接有Flac格式的，这还有待研究。

* ### 播放
  * 使用Android自带的MediaPlayer API进行播放

## TODO
* 实时转码Flac研究
* codereview代码细节容错等
