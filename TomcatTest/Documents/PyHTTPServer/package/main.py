#-*- coding:utf-8 -*-
'''
Created on 2015年4月9日

@author: Acer
'''

import BaseHTTPServer
from urlparse import urlparse,parse_qs
import Analyser
PORT = 4399

class myHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type","text/html")
        self.send_header("Accept-charset","utf-8")
        self.end_headers()
        print self.path
        param = parse_qs(urlparse(self.path).query)
        #http://localhost:8080/?keyWords=%E8%8B%8F%E8%8F%B2&list=123123
        #{'keyWords': [''], 'list': ['123123']}
        if not param.has_key("keyWords") or not param.has_key("jsonList"):
            self.wfile.write("Illegal parameters")
            return 
        if not param.has_key("callback"):
            result = Analyser.analyseWithoutExtraData(param)
            self.wfile.write(result)
        else:
            result = Analyser.analyseWithExtraData(param)
            callback = param["callback"][0]
            print result
            script = 'setTimeout(function(){%s(%s);},5000);' %(callback,result)
            print script
            self.wfile.write(script)
        return
    
def __main__():
    addr = ("",PORT)
    server=BaseHTTPServer.HTTPServer(addr,myHandler)
    server.serve_forever()

if __name__ == "__main__":
    __main__()
