#napomene:

- Testirano samo na Samsung Galaxy S3 Neo , Android 4.4.2
- U Image mode, ako se stisne Recognize, a nije ucitana slika, aplikacija ce se crashovati
- U Camera mode, lica se dodaju u bazu tek kada se stisne End Training
- U Camera mode, kod treniranja, kad je lice detektovano i vidi se u malom prozoru levo, treba stiskati 'save'[disketa] button dok ne iskoci dijalog 
- Pri treniranju u Image modu, mora biti tacno 1 lice detektovano
- Pri prepoznavanju, broj pored imena [confidence], sto je blizi 0 to bolje
- Potreban je OpenCV Manager da bi aplikacija radila, ako ga nema, iskoci prompt za instalaciju koji vodi na play store
