package org.dhatim.fastexcel;

import java.io.IOException;

/**
 * @author Peter Wu
 */
public class AbsoluteListDataValidation implements DataValidation {

  private final String list;

  private final static String TYPE = "list";
  private final Range range;

  private boolean allowBlank = true;
  private boolean showDropdown = true;
  private DataValidationErrorStyle errorStyle = DataValidationErrorStyle.INFORMATION;
  private boolean showErrorMessage = false;
  private String errorTitle;
  private String error;

  /**
   * Constructor
   *
   * @param range The Range this validation is applied to
   * @param list  The list this validation
   */
  public AbsoluteListDataValidation(Range range, String list) {
    this.range = range;
    this.list = list;
  }

  /**
   * whether blank cells should pass the validation
   *
   * @param allowBlank whether or not to allow blank values
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation allowBlank(boolean allowBlank) {
    this.allowBlank = allowBlank;
    return this;
  }

  /**
   * Whether Excel will show an in-cell dropdown list containing the validation list
   *
   * @param showDropdown whether or not to show the dropdown
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation showDropdown(boolean showDropdown) {
    this.showDropdown = showDropdown;
    return this;
  }

  /**
   * The style of error alert used for this data validation.
   *
   * @param errorStyle The DataValidationErrorStyle for this DataValidation
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation errorStyle(DataValidationErrorStyle errorStyle) {
    this.errorStyle = errorStyle;
    return this;
  }

  /**
   * Whether to display the error alert message when an invalid value has been entered.
   *
   * @param showErrorMessage whether to display the error message
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation showErrorMessage(boolean showErrorMessage) {
    this.showErrorMessage = showErrorMessage;
    return this;
  }

  /**
   * Title bar text of error alert.
   *
   * @param errorTitle The error title
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation errorTitle(String errorTitle) {
    this.errorTitle = errorTitle;
    return this;
  }

  /**
   * Message text of error alert.
   *
   * @param error The error message
   * @return this ListDataValidation
   */
  public AbsoluteListDataValidation error(String error) {
    this.error = error;
    return this;
  }

  /**
   * Write this dataValidation as an XML element.
   *
   * @param w Output writer.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  public void write(Writer w) throws IOException {
    w
        .append("<dataValidation sqref=\"")
        .append(range.toString())
        .append("\" type=\"")
        .append(TYPE)
        .append("\" allowBlank=\"")
        .append(String.valueOf(allowBlank))
        .append("\" showDropDown=\"")
        .append(String
            .valueOf(!showDropdown)) // for some reason, this is the inverse of what you'd expect
        .append("\" errorStyle=\"")
        .append(errorStyle.toString())
        .append("\" showErrorMessage=\"")
        .append(String.valueOf(showErrorMessage))
        .append("\" errorTitle=\"")
        .append(errorTitle)
        .append("\" error=\"")
        .append(error)
        .append("\"><formula1>\"")
        .append(list)
        .append("\"</formula1></dataValidation>");
  }

  public void add(Worksheet sheet) {
    sheet.addValidation(this);
  }
}
