# -*- coding:utf-8 -*-

# extra data(LikeNum & readNum) invisible
def analyseWithoutExtraData(param):
	keyWords = param["keyWords"]
	jsonList = param["jsonList"]
	print "keyWords", keyWords[0]
	print "jsonList", jsonList[0]
	sample = '[{"Name":"cctv1","Url":"www.cctv1.com","Identity":"weixin_cctv1","Info":"一个小孩坐飞机","Stars":9},{"Name":"cctv2","Url":"www.cctv2.com","Identity":"weixin_cctv2","Info":"两个小孩梳小辫","Stars":7},{"Name":"cctv3","Url":"www.cctv3.com","Identity":"weixin_cctv3","Info":"三个小孩吃饼干","Stars":8},{"Name":"cctv4","Url":"www.cctv4.com","Identity":"weixin_cctv4","Info":"四个小孩写大字","Stars":5}]'	
	return sample

# extra data visible
def analyseWithExtraData(param):
	keyWords = param["keyWords"]
	jsonList = param["jsonList"]
	print "keyWords", keyWords[0]
	print "jsonList", jsonList[0]
	sample = '[{"Name":"cctv1","Url":"www.cctv1.com","Identity":"weixin_cctv1","Info":"一个小孩坐飞机","Stars":9},{"Name":"cctv2","Url":"www.cctv2.com","Identity":"weixin_cctv2","Info":"两个小孩梳小辫","Stars":7},{"Name":"cctv3","Url":"www.cctv3.com","Identity":"weixin_cctv3","Info":"三个小孩吃饼干","Stars":8},{"Name":"cctv4","Url":"www.cctv4.com","Identity":"weixin_cctv4","Info":"四个小孩写大字","Stars":5},{"Name":"cctv1","Url":"www.cctv1.com","Identity":"weixin_cctv1","Info":"一个小孩坐飞机","Stars":9},{"Name":"cctv2","Url":"www.cctv2.com","Identity":"weixin_cctv2","Info":"两个小孩梳小辫","Stars":7},{"Name":"cctv3","Url":"www.cctv3.com","Identity":"weixin_cctv3","Info":"三个小孩吃饼干","Stars":8},{"Name":"cctv4","Url":"www.cctv4.com","Identity":"weixin_cctv4","Info":"四个小孩写大字","Stars":5}]'	
	return sample

