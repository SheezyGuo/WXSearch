#-*- coding:utf-8 -*-
import socket

def __main__():
	sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
	sock.connect(("localhost",4399))
	sock.send("成都火锅#end")
	print sock.recv(1024)
	sock.close()

if __name__ == "__main__":
	__main__()