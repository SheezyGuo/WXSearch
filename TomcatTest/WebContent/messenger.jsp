<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="team.xinyuan519.VSearch.utils.*,java.net.URLDecoder"%>
<%
	String keyWords = URLDecoder.decode(request.getParameter("keyWords").trim(),"utf-8");
	String callback = URLDecoder.decode(request.getParameter("callback").trim(),"utf-8");
	DivGetter getter = new DivGetter(keyWords);
	String listConent = getter.getHTMLCodeByHttp(String.format(
			"http://%s:%d/?keyWords=%s&callback=%s",
			EnvironmentInfo.pythonServerIP,
			EnvironmentInfo.pythonServerPort, keyWords,callback));
	out.write(listConent);
%>