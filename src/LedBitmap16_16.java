import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * Matrix 7219 LED 显示16*16的汉字
 * 
 */
public class LedBitmap16_16
{

	private static final boolean debug = false;
	private static final int speed = 30;// 演示时间

	public static class Constants
	{
		public static byte MAX7219_REG_NOOP = 0x0;
		public static byte MAX7219_REG_DIGIT0 = 0x1;
		public static byte MAX7219_REG_DIGIT1 = 0x2;
		public static byte MAX7219_REG_DIGIT2 = 0x3;
		public static byte MAX7219_REG_DIGIT3 = 0x4;
		public static byte MAX7219_REG_DIGIT4 = 0x5;
		public static byte MAX7219_REG_DIGIT5 = 0x6;
		public static byte MAX7219_REG_DIGIT6 = 0x7;
		public static byte MAX7219_REG_DIGIT7 = 0x8;
		public static byte MAX7219_REG_DECODEMODE = 0x9;
		public static byte MAX7219_REG_INTENSITY = 0xA;
		public static byte MAX7219_REG_SCANLIMIT = 0xB;
		public static byte MAX7219_REG_SHUTDOWN = 0xC;
		public static byte MAX7219_REG_DISPLAYTEST = 0xF;
	}

	protected static final short NUM_DIGITS = 8;// 表示一个字符字模使用8个字节
	protected static final short NUM_DIGITS_16 = 32;// 16*16的点阵使用32个字节显示一个 单元

	// 重叠几个屏幕（>=1）
	protected short cascaded = 1;
	// 旋转方向（0,90,180,270）
	protected int orientation;
	// 保存显示的字节数组（缓冲区）
	protected byte[] buffer_up;// 上面的四个屏幕的缓存
	protected byte[] buffer_down;// 下面屏幕
	// SPI设备
	protected SpiDevice spi;

	public static void main(String[] args)
	{
		/*
		 * 8块显示屏，文字采用16*16的分辨率。排列方式为： 0 1 2 3->接4的左侧
		 * 
		 * 4 5 6 7->右侧接MCU
		 * 
		 */
		LedBitmap16_16 led = new LedBitmap16_16((short) 8);

		// 打开设备
		led.open();

		// 旋转270度，缺省两个屏幕是上下排列，我需要的是左右排
		led.orientation(90);
		led.showMessag_16_16(
				"大胖 派我来巡山,121^*(^&$*^%,今年第22号台风“海马”的 中心已于21于日中午12点40分前后在广东省汕尾市海丰县鲘门镇登陆，登陆时中心附近最大风力有14级（42米/秒，强台风级），中心最低气压为960百帕。“海马”直扑广东，带来严重风雨影响。昨天上午，广东中东部、福建东南部的部分地区瞬时最大风力有8～10级，广东东部沿海地区和岛屿局地有11～14级，广东汕尾浮标站达16级（52.9米/秒）");
		try
		{
			System.in.read();
			led.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// short[] buffer_up = new short[32];
		// LedBitmap16_16 led = new LedBitmap16_16((short) 8);
		// for (int i = 0; i < buffer_up.length; i++)
		// {
		// buffer_up[i] = (short) i;
		// System.out.print(buffer_up[i]);
		// }
		// short[] new_buffer = led.SetOddScreen(buffer_up);
		// System.out.println("*******");
		// for (int i = 0; i < new_buffer.length; i++)
		// {
		// new_buffer[i] = (short) i;
		// }
		//
		// short[] new_buffer_128 = new short[128];
		// System.arraycopy(new_buffer, 0, new_buffer_128, 0, 64);
		// System.arraycopy(new_buffer, 0, new_buffer_128, 64, 64);
		//
		// byte[] scr = led.splitIntoTwoScreen(new_buffer_128);
		// for (int i = 0; i < scr.length; i++)
		// {
		// System.out.print(" ");
		// System.out.print(scr[i]);
		// }
	}

	public boolean showInMatrixLed(String msg)
	{
		/* 8个点阵，上下个两个 */
		LedBitmap16_16 led = new LedBitmap16_16((short) 8);
		/* 打开设备 */
		led.open();

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
		led.orientation(90);
		led.showMessag_16_16(msg);
		return true;// 显示完成，返回true
	}

	/**
	 * 使用16*32，即2*4块点阵，显示16*16大小单元的汉字等，并滚动
	 * 
	 * 注意：1234后硬件连接5678，实际后者放在前者下面
	 * 
	 * @param text
	 * @param font
	 */
	public void showMessag_16_16(String text)
	{
		/* 调用MyString的函数，获取点阵字模缓存，同时处理并分割！ */
		// for (int i = 0; i < 4; i++)
		// {
		// text += " ";
		// }
		short[] values = new MyStrings().getStringBuffer(text);

		/* 处理values的数据，是数据长度为64个，即偶数个屏幕字段 */
		values = SetOddScreen(values);

		/* 将values中的数据分割成上下两行保存到src中 */
		byte[] src = splitIntoTwoScreen(values);

		/* 以8个位单位，数组旋转3*90度 ,从左向右扫描，变成从下到上扫描 */
		src = rotate90(src);

		for (int i = 0; i < src.length - 32;)// 依次发送缓存中的数据
		{
			try
			{
				Thread.sleep(speed);
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
			this.scroll_left(false);
			// Up:0-31
			this.buffer_up[this.buffer_up.length - 1] = src[i];//
																// 将数据转移到缓存中，并发送缓存
			// Down:32-63
			this.buffer_down[this.buffer_down.length - 1] = src[i + 32];// 将数据转移到缓存中，并发送缓存

			this.flush();
			if (i % 32 == 31)
				i += 33;
			else
				i++;
		}
	}

	/* 以8个位单位，数组旋转3*90度 ,从左向右扫描，变成从下到上扫描 */
	private byte[] rotate90(byte[] buf)
	{
		byte[] resultBuf = buf;
		byte[] tempBuf = new byte[8];
		for (int i = 0; i < resultBuf.length / 8; i++)
		{
			System.arraycopy(resultBuf, i * 8, tempBuf, 0, 8);
			tempBuf = _rotate_8_8(tempBuf);
			tempBuf = _rotate_8_8(tempBuf);
			tempBuf = _rotate_8_8(tempBuf);
			System.arraycopy(tempBuf, 0, resultBuf, i * 8, 8);
		}
		return resultBuf;

	}

	public LedBitmap16_16(short cascaded)
	{
		this.orientation = 0;
		this.cascaded = cascaded;
		this.buffer_up = new byte[NUM_DIGITS * this.cascaded / 2];
		this.buffer_down = new byte[NUM_DIGITS * this.cascaded / 2];

		try
		{
			if (!debug)
			{
				this.spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED,
						SpiDevice.DEFAULT_SPI_MODE);

				command(Constants.MAX7219_REG_SCANLIMIT, (byte) 0x7);
				command(Constants.MAX7219_REG_DECODEMODE, (byte) 0x0);
				command(Constants.MAX7219_REG_DISPLAYTEST, (byte) 0x0);
				// command(Constants.MAX7219_REG_SHUTDOWN, (byte) 0x1);

				this.brightness((byte) 3);
				// this.clear();
			}

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 发送指令 data 到寄存器 register
	 */
	public void command(byte register, byte data) throws Exception
	{

		int len = 2 * this.cascaded;
		byte[] buf = new byte[len];

		for (int i = 0; i < len; i += 2)
		{
			buf[i] = register;
			buf[i + 1] = data;
		}

		this._write(buf);
	}

	/**
	 * 清除屏幕
	 */
	public void clear()
	{

		try
		{
			for (int i = 0; i < this.cascaded; i++)
			{
				for (short j = 0; j < NUM_DIGITS; j++)
				{
					this._setbyte(i, (short) (j + Constants.MAX7219_REG_DIGIT0), (byte) 0x00);
				}
			}
			this.flush();

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 左右旋转
	 */
	public void rotate_left(boolean redraw)
	{
		byte t_u = this.buffer_up[NUM_DIGITS * this.cascaded / 2 - 1];
		byte t_d = this.buffer_down[NUM_DIGITS * this.cascaded / 2 - 1];
		for (int i = NUM_DIGITS * this.cascaded / 2 - 1; i > 0; i--)
		{
			this.buffer_up[i] = this.buffer_up[i - 1];
			this.buffer_down[i] = this.buffer_down[i - 1];
		}
		this.buffer_up[0] = t_u;
		this.buffer_down[0] = t_d;

		if (redraw)
			this.flush();
	}

	public void rotate_right(boolean redraw)
	{
		byte t_u = this.buffer_up[0];
		byte t_d = this.buffer_down[0];
		for (int i = 0; i < NUM_DIGITS * this.cascaded / 2 - 1; i++)
		{
			this.buffer_up[i] = this.buffer_up[i + 1];
			this.buffer_down[i] = this.buffer_down[i + 1];
		}
		this.buffer_up[NUM_DIGITS * this.cascaded - 1] = t_u;
		this.buffer_down[NUM_DIGITS * this.cascaded - 1] = t_d;

		if (redraw)
			this.flush();
	}

	/**
	 * 左右移动
	 */
	public void scroll_left(boolean redraw)
	{
		for (int i = 0; i < NUM_DIGITS * this.cascaded / 2 - 1; i++)
		{
			this.buffer_up[i] = this.buffer_up[i + 1];
			this.buffer_down[i] = this.buffer_down[i + 1];
		}
		this.buffer_up[NUM_DIGITS * this.cascaded / 2 - 1] = 0x0;
		this.buffer_down[NUM_DIGITS * this.cascaded / 2 - 1] = 0x0;

		if (redraw)
			this.flush();
	}

	public void scroll_right(boolean redraw)
	{
		for (int i = NUM_DIGITS * this.cascaded / 2 - 1; i > 0; i--)
		{
			this.buffer_up[i] = this.buffer_up[i - 1];
			this.buffer_down[i] = this.buffer_down[i - 1];
		}
		this.buffer_up[0] = 0x0;
		this.buffer_down[0] = 0x0;

		if (redraw)
			this.flush();
	}

	/**
	 * 旋转
	 */
	public void orientation(int angle)
	{
		this.orientation(angle, true);
	}

	public void orientation(int angle, boolean redraw)
	{
		if (angle != 0 && angle != 90 && angle != 180 && angle != 270)
			return;

		this.orientation = angle;
		if (redraw)
			this.flush();
	}

	/**
	 * 显示字符
	 */
	public void letter(short deviceId, short asciiCode)
	{
		this.letter(deviceId, asciiCode, Font.CP437_FONT, true);
	}

	public void letter(short deviceId, short asciiCode, short[][] font)
	{
		this.letter(deviceId, asciiCode, font, true);
	}

	public void letter(short deviceId, short asciiCode, short[][] font, boolean redraw)
	{
		short[] values = Font.value(font, asciiCode);

		short col = Constants.MAX7219_REG_DIGIT0;
		for (short value : values)
		{
			if (col > Constants.MAX7219_REG_DIGIT7)
				return;

			this._setbyte(deviceId, col, (byte) (value & 0xff));
			col += 1;
		}

		if (redraw)
			this.flush();
	}

	/*
	 * 将16*16点阵的数据分割，成上下两个点阵单元显示。
	 * 
	 * 即分割到1256一组，3478一组。
	 */
	// _screen:------>val:---------->_scr:
	// 1|2|3|4---------> 0-15*|32-47*---> 0- 7| 8-15|16-23|24-31
	// 5|6|7|8--------->16-31*|48-63*--->32-29|40-47|48-55|56-63
	//
	//
	//
	private byte[] splitIntoTwoScreen(short[] val)
	{
		if (val.length % 64 != 0)
			return null;
		int len = val.length;
		byte[] src = new byte[len];

		for (int i = 0; i < src.length / 64; i++)
		{
			/* 实际数据测试见excel表格 */
			/* 1 2号屏幕点阵 */
			for (int j = 0; j < 8; j++)// scr:0- 7| 8-15<-----value:0-15*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j + i * 64]);// 1#屏幕
			}
			for (int j = 8; j < 16; j++)// scr:0- 7| 8-15<-----value:0-15*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 15 + i * 64]);// 2#
			}

			/* 3 4号屏幕点阵 */
			for (int j = 16; j < 24; j++)// scr:16-23|24-31<-----val:32-47*
			{
				src[j + i * 64] = (byte) (0xff & val[j * 2 + i * 64]);// 3#
			}
			for (int j = 24; j < 32; j++)// scr:16-23|24-31<-----val:32-47*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 15 + i * 64]);// 4#（j-24)*2+33=2*j-15
			}

			/* 5 6号屏幕点阵 */
			for (int j = 32; j < 40; j++)// scr:32-39|40-47<-----16-31*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 48 + i * 64]);// 5#
			}
			for (int j = 40; j < 48; j++)// scr:32-39|40-47<-----16-31*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 63 + i * 64]);// 6#
																			// 2*j-63
			}

			/* 7 8号屏幕点阵:从val中直接发送到对应的src中 */
			for (int j = 48; j < 56; j++)// scr:48-55|56-63<-----val:48-63*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 48 + i * 64]);// 7#
			}
			for (int j = 56; j < 64; j++)// scr:48-55|56-63<-----val:48-63*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 63 + i * 64]);// 8#
																			// 2*j-63
			}

		}

		return src;
	}

	public short[] SetOddScreen(short[] val)
	{
		short[] add_val = new short[32];// 初始值是0
		short[] odd_val = new short[val.length + 32];

		if (val.length % 64 != 0)// 不是64的倍数（是32的倍数）
		{
			System.arraycopy(val, 0, odd_val, 0, val.length);
			System.arraycopy(add_val, 0, odd_val, val.length, add_val.length);
			return odd_val;
		} else
			return val;
	}

	/**
	 * 将缓存区数据写入设备
	 */
	public void flush()
	{
		try
		{
			byte[] buf_up = this.buffer_up;
			byte[] buf_down = this.buffer_down;

			if (this.orientation > 0)
			{
				buf_up = this._rotate(buf_up);
				buf_down = this._rotate(buf_down);
			}

			for (short pos = 0; pos < NUM_DIGITS; pos++)
			{
				// if (pos < 4)// up
				// this._write(this._values(pos, buf_up));
				// else // down
				// this._write(this._values(pos, buf_down));
				this._write(this._values(pos, buf_up, buf_down));
			}

		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 设置亮度 intensity的范围为0<=?<16
	 */
	public void brightness(byte intensity)
	{
		try
		{
			if (intensity < 0 || intensity > 15)
				return;

			this.command(Constants.MAX7219_REG_INTENSITY, intensity);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 关闭设备
	 */
	public void close()
	{
		try
		{
			this.clear();
			this.command(Constants.MAX7219_REG_SHUTDOWN, (byte) 0x0);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * 打开设备
	 */
	public void open()
	{
		try
		{
			this.command(Constants.MAX7219_REG_SHUTDOWN, (byte) 0x1);
			this.clear();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 将buf写入设备
	 */
	private void _write(byte[] buf) throws Exception
	{
		if (!debug)
		{
			// for(byte b:buf) System.out.print(b+",");
			// System.out.println("");

			this.spi.write(buf);
		} else
		{
			for (byte b : buf)
				System.out.print(b + ",");
			System.out.println("");
		}
	}

	/**
	 * 根据屏幕多少，构造出待写入的字节数组 返回格式为 [position,data,position1,data1],（以两块屏幕为例）
	 */
	private byte[] _values(short position, byte[] buf_up, byte[] buf_down) throws Exception
	{
		int len = 2 * this.cascaded;
		byte[] ret = new byte[len];

		for (int i = 0; i < this.cascaded; i++)
		{
			ret[2 * i] = (byte) ((position + Constants.MAX7219_REG_DIGIT0) & 0xff);
			if (i < 4)
				ret[2 * i + 1] = buf_up[(i * NUM_DIGITS) + position];
			else
				ret[2 * i + 1] = buf_down[((i - 4) * NUM_DIGITS) + position];
		}
		return ret;
	}

	/**
	 * 根据屏幕多少，构造出待写入的字节数组 返回格式为 [position,data,position1,data1],（以两块屏幕为例）
	 */
	private byte[] _values(short position, byte[] buf) throws Exception
	{
		int len = 2 * this.cascaded;
		byte[] ret = new byte[len];

		for (int i = 0; i < this.cascaded; i++)
		{
			ret[2 * i] = (byte) ((position + Constants.MAX7219_REG_DIGIT0) & 0xff);
			ret[2 * i + 1] = buf[((i * NUM_DIGITS) + position)];
		}
		return ret;
	}

	/**
	 * 写数据到显示缓冲区
	 */
	private void _setbyte(int deviceId, short position, byte value)
	{
		if (deviceId < 4)// up
		{
			int offset = deviceId * NUM_DIGITS + position - Constants.MAX7219_REG_DIGIT0;
			this.buffer_up[offset / 2] = value;
		} else
		{

			int offset = deviceId * NUM_DIGITS + position - Constants.MAX7219_REG_DIGIT0;
			this.buffer_down[offset / 2] = value;
		}
	}

	/**
	 * 8*8的数组旋转
	 */
	private byte[] _rotate_8_8(byte[] buf)
	{
		byte[] result = new byte[8];
		for (int i = 0; i < 8; i++)
		{ // 输出结果的索引
			short b = 0;
			short t = (short) ((0x01 << i) & 0xff); // 根据索引，计算一下需要取源的哪个bit
			for (int j = 0; j < 8; j++)
			{
				int d = 7 - i - j; // 计算一下移位的个数，与i有关，有可能为负数
				if (d > 0)
					b += (short) ((buf[j] & t) << d);
				else
					b += (short) ((buf[j] & t) >> (-1 * d));
			}
			result[i] = (byte) b;
		}

		return result;
	}

	/**
	 * 显示缓冲区旋转,每个屏幕单独旋转
	 */
	private byte[] _rotate(byte[] buf)
	{
		byte[] result = new byte[this.buffer_up.length];
		for (int i = 0; i < this.cascaded * NUM_DIGITS / 2; i += NUM_DIGITS)
		{
			byte[] tile = new byte[NUM_DIGITS];
			for (int j = 0; j < NUM_DIGITS; j++)
			{
				tile[j] = buf[i + j];
			}
			int k = this.orientation / 90;
			for (int j = 0; j < k; j++)
			{
				tile = this._rotate_8_8(tile);
			}
			for (int j = 0; j < NUM_DIGITS; j++)
			{
				result[i + j] = tile[j];
			}

		}

		return result;
	}

}
