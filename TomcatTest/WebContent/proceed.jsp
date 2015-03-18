<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="team.xinyuan519.wxsearch.utils.*" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>鸡脖搜索-。-</title>
</head>
<body>
<%
	String keyWords = request.getParameter("keyWords");
	KeyWordsHandler handler = new KeyWordsHandler();
	String result = handler.handle(keyWords);
	//Analyser analyser = new Analyser();
	//String[] rank = analyser.analyse(keyWords);
	//for(String info : rank){
	//	response.getWriter().write("<p>"+info+"</p>");
	//}
	response.getWriter().write(result);
%>
</body>
</html>