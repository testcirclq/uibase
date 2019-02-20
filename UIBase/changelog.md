1、仿IOS滑动返回，支持fragment跟Activity
2、activity跟fragment 支持混合跳转
3、activity跟fragment 的生命周期修改成 onBaseStart，onBaseResume，onBasePause ， onBaseStop
4、支持在跳转页面期间不支持点触事件，这样可以避免很多问题，如多次点击，造成2个页面问题，【不需要自己调用】
5、支持输入法弹窗点击任何空白界面，会关闭弹窗，关闭页面会关闭输入法弹窗【不需要自己调用】
6、支持沉浸式