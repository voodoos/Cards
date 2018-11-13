PYTHON=python

.PHONY: all android lily py clean clean_midi copy

all: android clean_midi

copy:
	cp -R gen/ProcessedSVG/*.xml ../app/src/main/res/drawable

android: lily
	java -jar Svg2VectorAndroid-1.0.jar gen > /dev/null 2>&1

lily: py
	$(MAKE) -C gen

py:
	${PYTHON} make_notes.py

clean:
	$(MAKE) -C gen clean
