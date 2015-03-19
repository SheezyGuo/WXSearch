<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="com.mongodb.DB,com.mongodb.DBCollection,com.mongodb.DBCursor,com.mongodb.DBObject,com.mongodb.MongoClient,team.xinyuan519.wxsearch.utils.*" %> 
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>XX搜索-。-</title>
</head>
<body>
<%
	String keyWords = request.getParameter("keyWords");
	Client client = new Client(keyWords,"localhost",4399,"utf-8");
	String result = client.sendMsg();
// 	Analyser analyser = new Analyser();
// 	Node n = new Node();
// 	Node[] nodes = analyser.analyse(keyWords);
// 	for(Node node : nodes){
// 		response.getWriter().write("<p>"+"微信号:"+node.getCollName()+"&nbsp;&nbsp;&nbsp;&nbsp;分数:<font color=\"red\">"+node.getScore()+"</font></p>");
// 	}
	response.getWriter().write(result);
%>
</body>
</html>