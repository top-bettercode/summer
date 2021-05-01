package com.baidu.ueditor;

import com.baidu.ueditor.define.ActionMap;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.State;
import com.baidu.ueditor.hunter.FileManager;
import com.baidu.ueditor.upload.IUploader;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class ActionEnter {

  private final ConfigManager configManager;

  public ActionEnter() {
    this.configManager = ConfigManager.getInstance();
  }

  public String exec(HttpServletRequest request, IUploader uploader) {

    String callbackName = request.getParameter("callback");

    if (callbackName != null) {

      if (!validCallbackName(callbackName)) {
        return new BaseState(false, AppInfo.ILLEGAL).toJSONString();
      }

      return callbackName + "(" + this.invoke(request, uploader) + ");";

    } else {
      return this.invoke(request, uploader);
    }

  }

  private String invoke(HttpServletRequest request, IUploader uploader) {

    String actionType = request.getParameter("action");
    if (actionType == null || !ActionMap.mapping.containsKey(actionType)) {
      return new BaseState(false, AppInfo.INVALID_ACTION).toJSONString();
    }

    if (this.configManager == null || !this.configManager.valid()) {
      return new BaseState(false, AppInfo.CONFIG_ERROR).toJSONString();
    }

    State state = null;

    int actionCode = ActionMap.getType(actionType);

    Map<String, Object> conf;

    switch (actionCode) {

      case ActionMap.CONFIG:
        return this.configManager.getAllConfig().toString();

      case ActionMap.UPLOAD_IMAGE:
      case ActionMap.UPLOAD_SCRAWL:
      case ActionMap.UPLOAD_VIDEO:
      case ActionMap.UPLOAD_FILE:
      case ActionMap.CATCH_IMAGE:
        conf = this.configManager.getConfig(actionCode);
        state = uploader.doExec(request, conf);
        break;
      case ActionMap.LIST_IMAGE:
      case ActionMap.LIST_FILE:
        conf = configManager.getConfig(actionCode);
        int start = this.getStartIndex(request);
        state = new FileManager(conf).listFile(start);
        break;

    }

    return state.toJSONString();

  }

  private int getStartIndex(HttpServletRequest request) {

    String start = request.getParameter("start");

    try {
      return Integer.parseInt(start);
    } catch (Exception e) {
      return 0;
    }

  }

  /**
   * callback参数验证
   */
  private boolean validCallbackName(String name) {
    return name.matches("^[a-zA-Z_]+[\\w0-9_]*$");
  }

}