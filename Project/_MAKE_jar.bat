javac -O -classpath .;looks.jar jFrame.java
pause
jar cvfm ../Jif.jar MANIFEST.MF *.class *.properties images/*.png
pause