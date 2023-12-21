package top.bettercode.summer.tools.excel

/**
 * Define horizontal alignment.
 * [here](https://msdn.microsoft.com/en-us/library/documentformat.openxml.spreadsheet.horizontalalignmentvalues(v=office.14).aspx).
 *
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
 *
 * https://learn.microsoft.com/en-us/previous-versions/office/developer/office-2010/cc802119(v=office.14)
 *
 *
 * Top 	Align Top. When the item is serialized out as xml, its value is "top".
 * 	Center 	Centered Vertical Alignment. When the item is serialized out as xml, its value is "center".
 * 	Bottom 	Aligned To Bottom. When the item is serialized out as xml, its value is "bottom".
 * 	Justify 	Justified Vertically. When the item is serialized out as xml, its value is "justify".
 * 	Distributed 	Distributed Vertical Alignment. When the item is serialized out as xml, its value is "distributed".
</pre> *
 */
enum class Alignment(val value: String) {

    /**
     * 上
     */
    TOP("top"),
    /**
     * 下
     */
    BOTTOM("bottom"),

    /**
     * 默认
     */
    GENERAL("general"),

    /**
     * 左对齐
     */
    LEFT("left"),

    /**
     * 居中
     */
    CENTER("center"),

    /**
     * 右对齐
     */
    RIGHT("right"),

    /**
     * 填充
     */
    FILL("fill"),

    /**
     * 两端对齐
     */
    JUSTIFY("justify"),

    /**
     * 中心连续
     */
    CENTER_CONTINUOUS("centerContinuous"),

    /**
     * 分散
     */
    DISTRIBUTED("distributed");

}