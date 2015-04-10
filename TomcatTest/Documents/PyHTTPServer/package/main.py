#-*- coding:utf-8 -*-
'''
Created on 2015年4月9日

@author: Acer
'''

import BaseHTTPServer
from urlparse import urlparse,parse_qs
from Analyser import analyse
PORT = 8080

class myHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type","text/html")
        self.end_headers()
        print self.path
        param = parse_qs(urlparse(self.path).query)
        #http://localhost:8080/?keyWords=%E8%8B%8F%E8%8F%B2&list=123123
        #{'keyWords': [''], 'list': ['123123']}
        if not param.has_key("keyWords") or not param.has_key("jsonList"):
            self.wfile.write("Illegal parameters")
            return 
        result = analyse(param)
        self.wfile.write(result)
        return
    
def __main__():
    addr = ("",PORT)
    server=BaseHTTPServer.HTTPServer(addr,myHandler)
    server.serve_forever()

if __name__ == "__main__":
    __main__()
