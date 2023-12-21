package top.bettercode.summer.tools.excel

/**
 *
 * @author Peter Wu
 */
class ByteArrayPicture(val data: ByteArray?,
                       /**
                        * @see org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_JPEG
                        */
                       val pictureType: Int)