import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * Matrix 7219 LED ��ʾ16*16�ĺ���
 * 
 */
public class LedBitmap16_16
{

	private static final boolean debug = false;
	private static final int speed = 30;// ��ʾʱ��

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

	protected static final short NUM_DIGITS = 8;// ��ʾһ���ַ���ģʹ��8���ֽ�
	protected static final short NUM_DIGITS_16 = 32;// 16*16�ĵ���ʹ��32���ֽ���ʾһ�� ��Ԫ

	// �ص�������Ļ��>=1��
	protected short cascaded = 1;
	// ��ת����0,90,180,270��
	protected int orientation;
	// ������ʾ���ֽ����飨��������
	protected byte[] buffer_up;// ������ĸ���Ļ�Ļ���
	protected byte[] buffer_down;// ������Ļ
	// SPI�豸
	protected SpiDevice spi;

	public static void main(String[] args)
	{
		/*
		 * 8����ʾ�������ֲ���16*16�ķֱ��ʡ����з�ʽΪ�� 0 1 2 3->��4�����
		 * 
		 * 4 5 6 7->�Ҳ��MCU
		 * 
		 */
		LedBitmap16_16 led = new LedBitmap16_16((short) 8);

		// ���豸
		led.open();

		// ��ת270�ȣ�ȱʡ������Ļ���������У�����Ҫ����������
		led.orientation(90);
		led.showMessag_16_16(
				"���� ������Ѳɽ,121^*(^&$*^%,�����22��̨�硰������ ��������21��������12��40��ǰ���ڹ㶫ʡ��β�к������������½����½ʱ���ĸ�����������14����42��/�룬ǿ̨�缶�������������ѹΪ960������������ֱ�˹㶫���������ط���Ӱ�졣�������磬�㶫�ж������������ϲ��Ĳ��ֵ���˲ʱ��������8��10�����㶫�����غ������͵���ֵ���11��14�����㶫��β����վ��16����52.9��/�룩");
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
		/* 8���������¸����� */
		LedBitmap16_16 led = new LedBitmap16_16((short) 8);
		/* ���豸 */
		led.open();

		/*
		 * ��ת90�ȣ�ԭʼ��Ӣ���ֿ�ʹ�õ��Ǵ����ң����ϵ��µ�ɨ�跽ʽ��Ϊ�˷��㻬����ʾ����һ�²��ԣ�
		 * 
		 * 1.���ֿ���ȡ��ģ��16*16�ֱ��ʣ�һ�����ֹ�32���ֽ�
		 * 
		 * 2.Ϊ�˷��㴦��������ģ����Ϊ64�ı�������2*4������Ĵ�С
		 * 
		 * 3.����ָ���ģ����val����src�У��任����ϵ��
		 * 
		 * 4.��8��λ��λ��������ת3*90�� ,��������ɨ�裬��ɴ��µ���ɨ��
		 * 
		 * 5.�����Ļ��ת���ͻ�����ʾscroll_left����src�ָ����������Ļ������buffer��
		 * 
		 * 6.��һ��ʱ�������ٶȿɵ�����ˢ����ʾ
		 */
		led.orientation(90);
		led.showMessag_16_16(msg);
		return true;// ��ʾ��ɣ�����true
	}

	/**
	 * ʹ��16*32����2*4�������ʾ16*16��С��Ԫ�ĺ��ֵȣ�������
	 * 
	 * ע�⣺1234��Ӳ������5678��ʵ�ʺ��߷���ǰ������
	 * 
	 * @param text
	 * @param font
	 */
	public void showMessag_16_16(String text)
	{
		/* ����MyString�ĺ�������ȡ������ģ���棬ͬʱ�����ָ */
		// for (int i = 0; i < 4; i++)
		// {
		// text += " ";
		// }
		short[] values = new MyStrings().getStringBuffer(text);

		/* ����values�����ݣ������ݳ���Ϊ64������ż������Ļ�ֶ� */
		values = SetOddScreen(values);

		/* ��values�е����ݷָ���������б��浽src�� */
		byte[] src = splitIntoTwoScreen(values);

		/* ��8��λ��λ��������ת3*90�� ,��������ɨ�裬��ɴ��µ���ɨ�� */
		src = rotate90(src);

		for (int i = 0; i < src.length - 32;)// ���η��ͻ����е�����
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
																// ������ת�Ƶ������У������ͻ���
			// Down:32-63
			this.buffer_down[this.buffer_down.length - 1] = src[i + 32];// ������ת�Ƶ������У������ͻ���

			this.flush();
			if (i % 32 == 31)
				i += 33;
			else
				i++;
		}
	}

	/* ��8��λ��λ��������ת3*90�� ,��������ɨ�裬��ɴ��µ���ɨ�� */
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
	 * ����ָ�� data ���Ĵ��� register
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
	 * �����Ļ
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
	 * ������ת
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
	 * �����ƶ�
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
	 * ��ת
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
	 * ��ʾ�ַ�
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
	 * ��16*16��������ݷָ��������������Ԫ��ʾ��
	 * 
	 * ���ָ1256һ�飬3478һ�顣
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
			/* ʵ�����ݲ��Լ�excel��� */
			/* 1 2����Ļ���� */
			for (int j = 0; j < 8; j++)// scr:0- 7| 8-15<-----value:0-15*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j + i * 64]);// 1#��Ļ
			}
			for (int j = 8; j < 16; j++)// scr:0- 7| 8-15<-----value:0-15*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 15 + i * 64]);// 2#
			}

			/* 3 4����Ļ���� */
			for (int j = 16; j < 24; j++)// scr:16-23|24-31<-----val:32-47*
			{
				src[j + i * 64] = (byte) (0xff & val[j * 2 + i * 64]);// 3#
			}
			for (int j = 24; j < 32; j++)// scr:16-23|24-31<-----val:32-47*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 15 + i * 64]);// 4#��j-24)*2+33=2*j-15
			}

			/* 5 6����Ļ���� */
			for (int j = 32; j < 40; j++)// scr:32-39|40-47<-----16-31*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 48 + i * 64]);// 5#
			}
			for (int j = 40; j < 48; j++)// scr:32-39|40-47<-----16-31*
			{
				src[j + i * 64] = (byte) (0xff & val[2 * j - 63 + i * 64]);// 6#
																			// 2*j-63
			}

			/* 7 8����Ļ����:��val��ֱ�ӷ��͵���Ӧ��src�� */
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
		short[] add_val = new short[32];// ��ʼֵ��0
		short[] odd_val = new short[val.length + 32];

		if (val.length % 64 != 0)// ����64�ı�������32�ı�����
		{
			System.arraycopy(val, 0, odd_val, 0, val.length);
			System.arraycopy(add_val, 0, odd_val, val.length, add_val.length);
			return odd_val;
		} else
			return val;
	}

	/**
	 * ������������д���豸
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
	 * �������� intensity�ķ�ΧΪ0<=?<16
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
	 * �ر��豸
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
	 * ���豸
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
	 * ��bufд���豸
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
	 * ������Ļ���٣��������д����ֽ����� ���ظ�ʽΪ [position,data,position1,data1],����������ĻΪ����
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
	 * ������Ļ���٣��������д����ֽ����� ���ظ�ʽΪ [position,data,position1,data1],����������ĻΪ����
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
	 * д���ݵ���ʾ������
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
	 * 8*8��������ת
	 */
	private byte[] _rotate_8_8(byte[] buf)
	{
		byte[] result = new byte[8];
		for (int i = 0; i < 8; i++)
		{ // ������������
			short b = 0;
			short t = (short) ((0x01 << i) & 0xff); // ��������������һ����ҪȡԴ���ĸ�bit
			for (int j = 0; j < 8; j++)
			{
				int d = 7 - i - j; // ����һ����λ�ĸ�������i�йأ��п���Ϊ����
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
	 * ��ʾ��������ת,ÿ����Ļ������ת
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
