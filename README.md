# [ccg-r3d](https://github.com/agdturner/ccg-r3d)
A Java library for rendering 3D spatial data based on [ccg-v3d]((https://github.com/agdturner/ccg-v3d)).

The aim is to support visualisation of models with [6 degrees of freedom](https://en.wikipedia.org/wiki/Six_degrees_of_freedom).

The focus is not on realistic rendering, but rendering geometry.

## Uses
* Helping explain and develop [ccg-v3d](https://github.com/agdturner/ccg-v3d) - the underlying 3D Euclidean geometry library.
* Supporting the development and integration of Environmental Digital Twins. The UK National Oceanography Centre have published [An Information Management Framework for Environmental Digital Twins (IMFe) Report](https://noc.ac.uk/files/documents/about/NOC%20IMFe%20Summary%20Report2.pdf) that explains what an Environmental Digital Twin is - basically it is a model of a region that provided feedback to the sensing and surveying of that region in order to more accurately and usefully represent it.

## Hello World!
Rendering of [Utah Teapot](https://en.wikipedia.org/wiki/Utah_teapot) using [Utah_teapot_(solid).stl](data/Utah_teapot_(solid).stl) with 9438 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -10), there is no shadow, there is effectively a low ambient light and a vector for a general light:

<img alt="A yellow scale rendering of the Utah Teapot" src="data/outputold/Utah_teapot_(solid)/oom=-10/lighting(i=0.2673_j=0.5345_k=0.8018)/Utah_teapot_(solid)_500x500_pt(i=-12.3089_j=13.0269_k=17.2189)_lighting(i=0.2673_j=0.5345_k=0.8018)_oom=-10.png" />

Rendering of the asteroid [Geographos](https://en.wikipedia.org/wiki/1620_Geographos) using a [Geographos 3D Model provided by NASA](https://nasa3d.arc.nasa.gov/detail/geographos) with 16380 triangles (resolution 500x375, [Order of Magnitude](https://en.wikipedia.org/wiki/Order_of_magnitude) of precision -8):

<img alt="A yellow scale rendering of Geographos" src="data/outputold/geographos/files/oom=-8/lighting(i=-0.2673_j=-0.5345_k=-0.8018)/1620geographos_500x500_pt(i=-3.3194_j=3.4588_k=-3.4339)_lighting(i=-0.2673_j=-0.5345_k=-0.8018)_oom=-8.png" />

The images of the Geographos and the Utah Teapot were produced by running [RenderImage.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/RenderImage.java). As the underlying library changed, running this file does not currently reproduce these images..

The image below is a rendering of [Hurricane Katrina](https://en.wikipedia.org/wiki/Hurricane_Katrina) using a [Hurricane_Katrina 3D Model provided by NASA](https://nasa3d.arc.nasa.gov/detail/hurricane-katrina):

<img alt="A yellow scale rendering of Hurricane Katrina" src="data/outputold/Hurricane_Katrina/files/epsilon=1.0E-7/lighting(i=-0.27_j=-0.53_k=-0.80)_ambientLight(0.05)/Katrina_1000x1000.png" />

The image of Hurricane Katrina was produced by running [RenderImageDouble.java](https://github.com/agdturner/ccg-r3d/tree/main/src/main/java/uk/ac/leeds/ccg/r3d/d/RenderImageDouble.java). As the underlying library changed, running this file does not currently reproduce these images..

## Development plans/ideas
- Generate reproducible results.

## Contributing
- Thanks for thinking about this.
- If this is to form into a collaborative project, it could do with a Code of Conduct and Contributor Guidelines based on something like this: [Open Source Guide](https://opensource.guide/)

## LICENSE
- [APACHE LICENSE, VERSION 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- Other licences are possible!

## Acknowledgements and thanks
- The [University of Leeds](http://www.leeds.ac.uk) has indirectly supported this work by employing me over the years and encouraging me to develop the skills necessary to produce this library.
- Thank you Eric for the [BigMath](https://github.com/eobermuhlner/big-math) library.

## Links
* [Various Printable 3D Models from NASA](https://nasa3d.arc.nasa.gov/models/printable) in [STL File Format](https://en.wikipedia.org/wiki/STL_(file_format)).
* [Wikipedia List_of_common_3D_test_models](https://en.wikipedia.org/wiki/List_of_common_3D_test_models).
