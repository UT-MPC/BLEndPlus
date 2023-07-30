#!/bin/bash

javac BLEDiscSimulator.java

java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n3_0d0m.properties    ./nihao/output_pkloss/old_nihao_003n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_003n_0d0m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n3_0d1m.properties    ./nihao/output_pkloss/old_nihao_003n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_003n_0d1m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n3_0d2m.properties    ./nihao/output_pkloss/old_nihao_003n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_003n_0d2m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n3_0d3m.properties    ./nihao/output_pkloss/old_nihao_003n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_003n_0d3m_100R.dc   500

java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n10_0d0m.properties   ./nihao/output_pkloss/old_nihao_010n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_010n_0d0m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n10_0d1m.properties   ./nihao/output_pkloss/old_nihao_010n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_010n_0d1m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n10_0d2m.properties   ./nihao/output_pkloss/old_nihao_010n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_010n_0d2m_100R.dc   500
java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n10_0d3m.properties   ./nihao/output_pkloss/old_nihao_010n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_010n_0d3m_100R.dc   500

#--------cannot find configurations for Nihao ----------
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n25_0d0m.properties   ./nihao/output_pkloss/old_nihao_025n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_025n_0d0m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n25_0d1m.properties   ./nihao/output_pkloss/old_nihao_025n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_025n_0d1m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n25_0d2m.properties   ./nihao/output_pkloss/old_nihao_025n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_025n_0d2m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n25_0d3m.properties   ./nihao/output_pkloss/old_nihao_025n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_025n_0d3m_100R.dc   500
#
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n50_0d0m.properties   ./nihao/output_pkloss/old_nihao_050n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_050n_0d0m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n50_0d1m.properties   ./nihao/output_pkloss/old_nihao_050n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_050n_0d1m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n50_0d2m.properties   ./nihao/output_pkloss/old_nihao_050n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_050n_0d2m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n50_0d3m.properties   ./nihao/output_pkloss/old_nihao_050n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_050n_0d3m_100R.dc   500
#
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n75_0d0m.properties   ./nihao/output_pkloss/old_nihao_075n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_075n_0d0m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n75_0d1m.properties   ./nihao/output_pkloss/old_nihao_075n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_075n_0d1m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n75_0d2m.properties   ./nihao/output_pkloss/old_nihao_075n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_075n_0d2m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n75_0d3m.properties   ./nihao/output_pkloss/old_nihao_075n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_075n_0d3m_100R.dc   500
#
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n100_0d0m.properties  ./nihao/output_pkloss/old_nihao_100n_0d0m_100R.log ./nihao/output_pkloss/dc_old_nihao_100n_0d0m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n100_0d1m.properties  ./nihao/output_pkloss/old_nihao_100n_0d1m_100R.log ./nihao/output_pkloss/dc_old_nihao_100n_0d1m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n100_0d2m.properties  ./nihao/output_pkloss/old_nihao_100n_0d2m_100R.log ./nihao/output_pkloss/dc_old_nihao_100n_0d2m_100R.dc   500
#java BLEDiscSimulator nihao/pklossWithOldMoldesSims/nihaosimulation_n100_0d3m.properties  ./nihao/output_pkloss/old_nihao_100n_0d3m_100R.log ./nihao/output_pkloss/dc_old_nihao_100n_0d3m_100R.dc   500