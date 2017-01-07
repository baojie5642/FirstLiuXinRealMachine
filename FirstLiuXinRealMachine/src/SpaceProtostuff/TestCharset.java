package SpaceProtostuff;

import java.util.Arrays;

public class TestCharset {

	/*
	 * Unicode、UTF－8 和 ISO8859-1到底有什么区别
	 * 
	 * 将以"中文"两个字为例，经查表可以知道其GB2312编码是"d6d0 cec4"，Unicode编码为"4e2d 6587"，UTF编码就是
	 * "e4b8ad e69687"。注意， 这两个字没有iso8859-1编码，但可以用iso8859-1编码来"表示"。
	 * 
	 * 2. 编码基本知识
	 * 
	 * 最早的编码是iso8859-1，和ascii编码相似。但为了方便表示各种各样的语言，逐渐出现了很多标准编码，重要的有如下几个。
	 * 
	 * 2.1. iso8859-1 通常叫做Latin-1
	 * 
	 * 属于单字节编码，最多能表示的字符范围是0-255，应用于英文系列。比如，字母a的编码为0x61=97。
	 * 
	 * 很明显，iso8859-1编码表示的字符范围很窄，无法表示中文字符。但是，由于是单字节编码，和计算机最基础的表示单位一致，所以很多时候，
	 * 仍旧使用iso8859
	 * -1编码来表示。而且在很多协议上，默认使用该编码。比如，虽然"中文"两个字不存在iso8859-1编码，以gb2312编码为例，应
	 * 该是"d6d0 cec4"
	 * 两个字符，使用iso8859-1编码的时候则将它拆开为4个字节来表示："d6 d0 ce c4"（事实上，在进行存储的时候，也是以字节为
	 * 单位处理的）。而如果是UTF编码，则是6个字节"e4 b8 ad e6 96 87"。很明显，这种表示方法还需要以另一种编码为基础。
	 * 
	 * 2.2. GB2312/GBK
	 * 
	 * 这就是汉子的国标码，专门用来表示汉字，是双字节编码，而英文字母和iso8859-1一致（兼容iso8859-1编码）。其中gbk编码能够用来同时表示
	 * 繁体字和简体字，而gb2312只能表示简体字，gbk是兼容gb2312编码的。
	 * 
	 * 2.3. unicode
	 * 
	 * 这是最统一的编码，可以用来表示所有语言的字符，而且是定长双字节（也有四字节的）编码，包括英文字母在内。所以可以说它是不兼容iso8859-1编
	 * 码的，也不兼容任何编码。不过，相对于iso8859-1编码来说，uniocode编码只是在前面增加了一个0字节，比如字母a为"00 61"。
	 * 
	 * 需要说明的是，定长编码便于计算机处理（注意GB2312/GBK不是定长编码），而unicode又可以用来表示所有字符，
	 * 所以在很多软件内部是使用unicode 编码来处理的，比如java。
	 * 
	 * 2.4. UTF
	 * 
	 * 考虑到unicode编码不兼容iso8859-1编码，而且容易占用更多的空间：因为对于英文字母，unicode也需要两个字节来表示。
	 * 所以unicode不便于传输和存
	 * 储。因此而产生了utf编码，utf编码兼容iso8859-1编码，同时也可以用来表示所有语言的字符，不过，utf编码是不定长编码
	 * ，每一个字符的长度从1-6个字 节不等。另外，utf编码自带简单的校验功能。一般来讲，英文字母都是用一个字节表示，而汉字使用三个字节。
	 * 
	 * 注意，虽然说utf是为了使用更少的空间而使用的，但那只是相对于unicode编码来说，如果已经知道是汉字，则使用GB2312/GBK无疑是最节省的。
	 * 不过另
	 * 一方面，值得说明的是，虽然utf编码对汉字使用3个字节，但即使对于汉字网页，utf编码也会比unicode编码节省，因为网页中包含了很多的英文字符
	 * 。
	 * 
	 * 3. java对字符的处理
	 * 
	 * 在java应用软件中，会有多处涉及到字符集编码，有些地方需要进行正确的设置，有些地方需要进行一定程度的处理。
	 * 
	 * 3.1. getBytes(charset)
	 * 
	 * 这是java字符串处理的一个标准函数，其作用是将字符串所表示的字符按照charset编码，并以字节方式表示。
	 * 注意字符串在java内存中总是按unicode编码
	 * 存储的。比如"中文"，正常情况下（即没有错误的时候）存储为"4e2d 6587"，如果charset为
	 * "gbk"，则被编码为"d6d0 cec4"，然后返回字节"d6 d0 ce c4"。
	 * 如果charset为"utf8"则最后是"e4 b8 ad e6 96 87"。如果是"iso8859-1"，则由于无法编码，最后返回
	 * "3f 3f"（两个问号）。
	 * 
	 * 3.2. new String(charset)
	 * 
	 * 这是java字符串处理的另一个标准函数，和上一个函数的作用相反，将字节数组按照charset编码进行组合识别，最后转换为unicode存储。
	 * 参考上述getBytes 的例子，"gbk"
	 * 和"utf8"都可以得出正确的结果"4e2d 6587"，但iso8859-1最后变成了"003f 003f"（两个问号）。
	 * 
	 * 因为utf8可以用来表示/编码所有字符，所以new String( str.getBytes( "utf8" ), "utf8" ) ===
	 * str，即完全可逆。
	 * 
	 * 3.3. setCharacterEncoding()
	 * 
	 * 该函数用来设置http请求或者相应的编码。
	 * 
	 * 对于request，是指提交内容的编码，指定后可以通过getParameter()则直接获得正确的字符串，如果不指定，则默认使用iso8859-1编码
	 * ，需要进一步处理。
	 * 参见下述"表单输入"。值得注意的是在执行setCharacterEncoding()之前，不能执行任何getParameter()。java
	 * doc上说明：This method must be called prior to reading request parameters or
	 * reading input using
	 * getReader()。而且，该指定只对POST方法有效，对GET方法无效。分析原因，应该是在执行第一个getParameter
	 * ()的时候，java将会按照编码分析所有的提交内容
	 * ，而后续的getParameter()不再进行分析，所以setCharacterEncoding(
	 * )无效。而对于GET方法提交表单是，提交的内容在URL中
	 * ，一开始就已经按照编码分析所有的提交内容，setCharacterEncoding()自然就无效。
	 * 
	 * 4.iso-8859-1是JAVA网络传输使用的标准 字符集,而gb2312是标准中文字符集,当你作出提交表单等需要网络传输的操作的时候,就需要把
	 * iso-8859-1转换为gb2312字符集显示,否则如果按浏览器的gb2312格式来解释iso-8859-1字符集的话,由于2者不兼容,所以会
	 * 是乱码.
	 */

	public static void main(String args[]) throws Exception {
		String s = "你好";
		// 编码
		byte[] utf = s.getBytes("utf-8");
		byte[] gbk = s.getBytes("gbk");
		System.out.println("utf-8编码：" + Arrays.toString(utf)); // [-28, -67,
																// -96, -27,
																// -91, -67]
																// 6个字节
		System.out.println("gbk编码：" + Arrays.toString(gbk)); // [-60, -29, -70,
																// -61]<span
																// style="white-space:pre">
																// </span>4个字节
		// 解码
		String s1 = new String(utf, "utf-8"); // 你好
		String s2 = new String(utf, "gbk");// gbk解码：浣犲ソ gbk用2个字节解码，所以会多一个字符
		String s3 = new String(gbk, "utf-8");// gbk用utf-8解码：??? <span
												// style="white-space:pre">
												// </span>utf-8解码需要6个字节
		System.out.println("--------------------");
		System.out.println("utf-8解码：" + s1);
		System.out.println("gbk解码：" + s2);
		System.out.println("gbk用utf-8解码：" + s3);
		System.out.println("---------------------");
		System.out.println("用utf-8编码回去");
		s3 = new String(s3.getBytes("utf-8"), "gbk"); // 锟斤拷锟? gbk用utf-8解码后无法编回去
		System.out.println(s3);
		/*
		 * 规律： utf-8编码可以用gbk和iso8859-1解码后编回去 gbk编码后只能用iso8859-1解码后编回去
		 * 
		 * 在JSP页面获取表单的值时会出现乱码，有两种解决方法：
		 * 
		 * 一种是在调用getParameter之前通过request.setCharacterEncoding设置字符编码， 另一种是调用new
		 * String(str.getBytes("iso8859-1"), "UTF-8");编码后解码， 这两种方法都可以得到正确的结果
		 */
	}
}
