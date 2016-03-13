#!/bin/bash

# stuff to fetch:
# ^0|^ 0         Gruppenwechsel für Zeile im Einzugs CSV
# MNDREF         Mandatenreferenz (ist auch im MOS hinterlegt)
# RÜCKLEITUNG    Der Einzug wurde anscheinend abgelehnt (Bouncer)
# E-BVB          IBAN des Kontos von dem eingezogen wurde
# EMPBIC         BIC des Kontos
# /DDT/U/        Steht am Beginn eines Einzugs im Header
# 9999999999999999999999999999999999999999   Zeile in der auch wieder die Einzugsnummer+Zeile im Einzug vorkommt
# PMD            Verwendungszweck ("Mitgliedsbeitrag ../..") 
# ANZUMS         Anzahl der Umsätze (aka von wie vielen Leuten wurde eingezogen)
#  K N \$        Name

# stuff to ignore:
# BASISVALUTA    Zeile die auch die 9er drinnen hat, wird aber nicht benötigt 

cat $1 | grep -E "  ANZUMS|^  PMD|^PMD  1|^   PMD  1|\/DDT\/U\/|^0|^ 0| K N \$|MNDREF|RÜCKLEITUNG|E-BVB|EMPBIC|9999999999999999999999999999999999999999" | grep -v "BASISVALUTA" 
