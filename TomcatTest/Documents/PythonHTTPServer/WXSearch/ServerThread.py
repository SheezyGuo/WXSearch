import threading
from Analyser import analyse
END_MARK = "#end"

class ServerThread(threading.Thread):
	def __init__(self,clientSocket,clientAddr):
		threading.Thread.__init__(self)
		self.clientSocket=clientSocket
		self.clientAddr=clientAddr

	def run(self):
		self.clientSocket.settimeout(60)
		print "Connect from",self.clientAddr
		request=""
		READ_OVER = False;
		while True:
			jsonStr=self.clientSocket.recv(1024)
			if len(jsonStr)==0:
				print "Socket from %s interrupt" % self.clientAddr
				READ_OVER = False
				break
			request+=jsonStr.rstrip()
			# if response[len(response)-4:len(response)]=="#end":
			print "Got request:\n"+request
			if request.endswith("#end"):				
				request=request.replace("#end","")
				READ_OVER = True
				break
		if not READ_OVER:
			self.clientSocket.send("READ_OVER:"+"False"+END_MARK)
			self.clientSocket.close()
			return 
		data=analyse(request)
		self.clientSocket.send(data+END_MARK)
		self.clientSocket.close()
