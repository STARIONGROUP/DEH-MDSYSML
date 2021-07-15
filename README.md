# DEH-MDSYSML

The DEH MDSYSML is a Java plugin Application that makes use of the [Comet SDKJ](https://github.com/RHEAGROUP/COMET-SDKJ-Community-Edition),
which is available on Maven Central.

The DEH MDSYSML allows users to interactivly exchange data between models built with the [MagicDraw SysML](hhttps://www.3ds.com/products-services/catia/products/no-magic/addons/sysml-plugin/) software and a ECSS-E-TM-10-25A data source.

## Build Instructions

- Copy then past all the Jar file located in ```{your Cameo/MagicDraw installation path}\lib\``` to ```{The location of the DEH-MDSYSML project repository}\lib\```.
- Generate a Jar named "DEHMDSYSMLPlugin", no need to include the references from the lib folder.
- Copy then paste the Jar file into ```{your Cameo/MagicDraw installation path}\plugins\com.rheagroup.dehmdsysml\```.
- Copy then paste the file named ```plugin.xml``` into ```{your Cameo/MagicDraw installation path}\plugins\com.rheagroup.dehmdsysml\```.

## License

The libraries contained in the DEH MDSYSML are provided to the community under the GNU Lesser General Public License. Because we make the software available with the LGPL, it can be used in both open source and proprietary software without being required to release the source code of your own components.

## Contributions

Contributions to the code-base are welcome. However, before we can accept your contributions we ask any contributor to sign the Contributor License Agreement (CLA) and send this digitaly signed to s.gerene@rheagroup.com. You can find the CLA's in the CLA folder.
