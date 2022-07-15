# DEH-MDSYSML

The DEH MDSYSML is a Java plugin Application that makes use of the [Comet SDKJ](https://github.com/RHEAGROUP/COMET-SDKJ-Community-Edition),
which is available on Maven Central. It allows users to interactivly exchange data between models built with the [MagicDraw SysML](hhttps://www.3ds.com/products-services/catia/products/no-magic/addons/sysml-plugin/) software and an ECSS-E-TM-10-25A data source.

The DEH MDSYSML can be used in MagicDraw™ with the SysML plugin and Cameo™ (Cameo Systems Modeler™ and Cameo Enterprise Architecture™), compatible versions are 19 SP3 and 19 SP4.

## Installing the DEH-MDSYSML adapter

- Download the [latest release](https://github.com/RHEAGROUP/DEH-MDSYSML/releases/latest).
- Close any running instance of MagicDraw™ or Cameo™.
- Unzip the plugin and/or copy the folder contained in the zip file into the plugin directory of the installation location of MagicDraw™ or Cameo™.
- Restart MagicDraw™ or Cameo™.
- The same step applies for updating the adapter.

## Operating the DEH-MDSYSML adapter

- After installing the adpter.
- A Comet icon ![Comet](https://github.com/RHEAGROUP/DEH-CommonJ/blob/master/src/main/resources/icon16.png?raw=true) in the main toolbar gives access to show/hide all the views of the adapter.
- The Hub panel is the one that allows to connect to a Comet webservice/ECSS-E-TM-10-25A data source. Once there is a Comet model open, and a SysML project open. Mapping between models can achieved in any direction.
- To initialize a new mapping, there is a Map action available in the context menus of Project browsers such as the ones from Cameo or MagicDraw and the ElementDefinitions and Requirements ones from the adapter panels.
- The Impact View panel is where Impact on target models can be previewed/transfered. Also from this view mapping information can be loaded/saved.
- The *Notification Window* displays the output of the adapter.

## License

The libraries contained in the DEH MDSYSML are provided to the community under the GNU Lesser General Public License. Because we make the software available with the LGPL, it can be used in both open source and proprietary software without being required to release the source code of your own components.

## Contributions

Contributions to the code-base are welcome. However, before we can accept your contributions we ask any contributor to sign the Contributor License Agreement (CLA) and send this digitaly signed to s.gerene@rheagroup.com. You can find the CLA's in the CLA folder.
