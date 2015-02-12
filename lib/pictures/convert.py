import Image
import os

path = './jpg/'

for im in os.listdir(path):
	if im.find('jpg'):
		print im	
		try:
			img = Image.open(path + im)
			img.thumbnail((256, 256), Image.ANTIALIAS)
			img.save('png/' + im[:-3] + 'png')
		except:
			print 'error'