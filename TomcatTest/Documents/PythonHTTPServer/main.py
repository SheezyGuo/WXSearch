import socket
import json
import sys,os
sys.path.append(os.getcwd()+os.sep+"WXSearch")
from ServerThread import ServerThread

def __main__():
	print "in main"
	sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
	addr=("",4399)
	sock.bind(addr)
	sock.listen(5)
	while True:
		clientSocket,clientAddr=sock.accept()
		t=ServerThread(clientSocket,clientAddr)
		t.start()
	sock.close()

if __name__ == "__main__":
	__main__()


