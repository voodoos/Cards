LILY=/Applications/LilyPond.app/Contents/Resources/bin/lilypond

.PHONY: all lily clean clean_midi

all: lily clean_midi

lily:
	${LILY} -dbackend=svg *.ly > /dev/null 2>&1
	sed -i '' 's/currentColor/black/' *.svg

clean: clean_midi
	rm -f *.ly
	rm -f *.svg
	rm -rf ProcessedSVG

clean_midi:
	rm -f *.midi