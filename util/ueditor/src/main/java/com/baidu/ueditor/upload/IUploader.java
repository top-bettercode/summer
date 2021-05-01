package com.baidu.ueditor.upload;

import com.baidu.ueditor.define.State;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public interface IUploader {


  State doExec(HttpServletRequest request, Map<String, Object> conf);


}
