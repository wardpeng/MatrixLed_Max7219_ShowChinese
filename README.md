# MatrixLed_Max7219_ShowChinese
感谢sharetop《树莓派研究:用Java实现Max7219 LED点阵的输出》，他的Git项目地址https://github.com/sharetop/max7219-java。
在这篇文章实现了8*8的assic字符显示，修改了部分代码，添加了16*16大小的常见汉字（可以选择使用多个字库，都在项目里）任意字符滚动显示。

打算结合我的其他文章，做个天气预报屏玩玩的。

	/*
		 * 旋转90度，原始中英文字库使用的是从左到右，从上到下的扫描方式。为了方便滑动显示采用一下策略：
		 * 
		 * 1.从字库中取字模，16*16分辨率，一个汉字共32个字节
		 * 
		 * 2.为了方便处理，整理字模长度为64的倍数，即2*4个点阵的大小
		 * 
		 * 3.处理分割字模，从val处理到src中（变换坐标系）
		 * 
		 * 4.以8个位单位，数组旋转3*90度 ,从左向右扫描，变成从下到上扫描
		 * 
		 * 5.配合屏幕旋转，和滑动显示scroll_left，将src分割到上下两组屏幕的两个buffer中
		 * 
		 * 6.以一定时间间隔（速度可调），刷新显示
*/
