package top.bettercode.summer.tools.excel

/**
 * Define horizontal alignment.
 * [here](https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx).
 * <pre>
 * Member name 	Description
 * General 	General Horizontal Alignment. When the item is serialized out as xml, its value is "general".
 * Left 	Left Horizontal Alignment. When the item is serialized out as xml, its value is "left".
 * Center 	Centered Horizontal Alignment. When the item is serialized out as xml, its value is "center".
 * Right 	Right Horizontal Alignment. When the item is serialized out as xml, its value is "right".
 * Fill 	Fill. When the item is serialized out as xml, its value is "fill".
 * Justify 	Justify. When the item is serialized out as xml, its value is "justify".
 * CenterContinuous 	Center Continuous Horizontal Alignment. When the item is serialized out as xml, its value is "centerContinuous".
 * Distributed 	Distributed Horizontal Alignment. When the item is serialized out as xml, its value is "distributed".
</pre> *
 */
enum class Alignment {
    /**
     * 默认
     */
    general,

    /**
     * 左对齐
     */
    left,

    /**
     * 居中
     */
    center,

    /**
     * 右对齐
     */
    right,

    /**
     * 填充
     */
    fill,

    /**
     * 两端对齐
     */
    justify,

    /**
     * 中心连续
     */
    centerContinuous,

    /**
     * 分散
     */
    distributed
}