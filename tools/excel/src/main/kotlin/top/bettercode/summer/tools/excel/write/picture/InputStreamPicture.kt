package top.bettercode.summer.tools.excel.write.picture

import java.io.InputStream

/**
 *
 * @author Peter Wu
 */
class InputStreamPicture(val data: InputStream?,
                         /**
                          * @see org.apache.poi.ss.usermodel.Workbook.PICTURE_TYPE_JPEG
                          */
                         val pictureType: Int)