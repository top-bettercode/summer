package org.dhatim.fastexcel

import java.io.IOException

/**
 * @author Peter Wu
 */
internal class AbsoluteListDataValidation
/**
 * Constructor
 *
 * @param range The Range this validation is applied to
 * @param list  The list this validation
 */(private val range: Range, private val list: String?) : DataValidation {
    private var allowBlank = true
    private var showDropdown = true
    private var errorStyle = DataValidationErrorStyle.INFORMATION
    private var showErrorMessage = false
    private var errorTitle: String? = null
    private var error: String? = null

    /**
     * whether blank cells should pass the validation
     *
     * @param allowBlank whether or not to allow blank values
     * @return this ListDataValidation
     */
    fun allowBlank(allowBlank: Boolean): AbsoluteListDataValidation {
        this.allowBlank = allowBlank
        return this
    }

    /**
     * Whether Excel will show an in-cell dropdown list containing the validation list
     *
     * @param showDropdown whether or not to show the dropdown
     * @return this ListDataValidation
     */
    fun showDropdown(showDropdown: Boolean): AbsoluteListDataValidation {
        this.showDropdown = showDropdown
        return this
    }

    /**
     * The style of error alert used for this data validation.
     *
     * @param errorStyle The DataValidationErrorStyle for this DataValidation
     * @return this ListDataValidation
     */
    fun errorStyle(errorStyle: DataValidationErrorStyle): AbsoluteListDataValidation {
        this.errorStyle = errorStyle
        return this
    }

    /**
     * Whether to display the error alert message when an invalid value has been entered.
     *
     * @param showErrorMessage whether to display the error message
     * @return this ListDataValidation
     */
    fun showErrorMessage(showErrorMessage: Boolean): AbsoluteListDataValidation {
        this.showErrorMessage = showErrorMessage
        return this
    }

    /**
     * Title bar text of error alert.
     *
     * @param errorTitle The error title
     * @return this ListDataValidation
     */
    fun errorTitle(errorTitle: String?): AbsoluteListDataValidation {
        this.errorTitle = errorTitle
        return this
    }

    /**
     * Message text of error alert.
     *
     * @param error The error message
     * @return this ListDataValidation
     */
    fun error(error: String?): AbsoluteListDataValidation {
        this.error = error
        return this
    }

    /**
     * Write this dataValidation as an XML element.
     *
     * @param w Output writer.
     * @throws IOException If an I/O error occurs.
     */
    override fun write(w: Writer) {
        w
                .append("<dataValidation sqref=\"")
                .append(range.toString())
                .append("\" type=\"")
                .append(TYPE)
                .append("\" allowBlank=\"")
                .append(allowBlank.toString())
                .append("\" showDropDown=\"")
                .append((!showDropdown).toString()) // for some reason, this is the inverse of what you'd expect
                .append("\" errorStyle=\"")
                .append(errorStyle.toString())
                .append("\" showErrorMessage=\"")
                .append(showErrorMessage.toString())
                .append("\" errorTitle=\"")
                .append(errorTitle)
                .append("\" error=\"")
                .append(error)
                .append("\"><formula1>\"")
                .append(list)
                .append("\"</formula1></dataValidation>")
    }

    fun add(sheet: Worksheet) {
        sheet.addValidation(this)
    }

    companion object {
        private const val TYPE = "list"
    }
}
