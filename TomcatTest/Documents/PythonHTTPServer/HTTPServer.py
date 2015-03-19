import socket
import json
import sys,os
sys.path.append(os.getcwd()+os.sep+"WXSearch")
from Analyser import analyze

END_MARK = "#end"

def __main__():
	print "in main"
	sock=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
	addr=("localhost",4399)
	sock.bind(addr)
	sock.listen(5)
	while True:
		clientSocket,clientAddr=sock.accept()
		print "Connect from",clientAddr
		request=""
		while True:
			jsonStr=clientSocket.recv(1024)
			request+=jsonStr
			# if response[len(response)-4:len(response)]=="#end":
			print "Got request:\n"+request
			if request.endswith("#end"):				
				request=request.replace("#end","")
				break
		data=analyze(request)
		# print data
		# send=[1,"2",(3,4),{"5":"6"},"Receive up to buffersize bytes from the socket.  For the optional argument, see the Unix manual.  When no data is available, block until at least one byte is available or until the remote end is closed.  When the remote end is closed and all data is read, return the empty string."]
		# print json.dumps(send)
		clientSocket.send(data+END_MARK)
		clientSocket.close()
	sock.close()

if __name__ == "__main__":
	# analyze("hello")
	__main__()


