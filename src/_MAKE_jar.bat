javac -source 1.4 -O -classpath . *.java
pause
jar cvfm ../Jif.jar MANIFEST.MF *.class *.properties images/*.png readme.txt Jif.cfg
pause
del *.class
pause