

notes = {'a', 'b', 'c', 'd', 'e', 'f', 'g'}

for base in notes:
    notesvars = {base, base + "is", base + "es"}
    for note in notesvars:
        name = "gen/note_" + note + ".ly"
        with open(name, "w") as file:
            file.write("\\version \"2.18.2\"\n")
            file.write(
                "#(set! paper-alist (cons '(\"tiny\" . (cons (* 2 cm) (* 2 cm))) paper-alist))\n")
            file.write("#(set-default-paper-size \"tiny\")\n")
            file.write("\\header { tagline = \"\"  }\n")
            file.write("\\score{\n")
            file.write("\\relative f' {\n")
            file.write("\\override Staff.TimeSignature.stencil =  ##f\n")
            file.write(note)
            file.write("}\n")
            file.write("\\layout{}\n")
            file.write("\\midi{}\n")
            file.write("}\n")
