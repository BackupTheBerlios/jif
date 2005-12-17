javac -O -classpath . jFrame.java
pause
jar cvfm ../Jif.jar MANIFEST.MF *.class *.properties images/*.png
pause
del *.class
pause