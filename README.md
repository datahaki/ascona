![alpine_877](https://user-images.githubusercontent.com/4012178/116814864-1b1a1580-ab5b-11eb-97e6-1441af4ececa.png)

# ch.alpine.ascona

Computational non-linear geometry demos in Java 17

![](https://github.com/datahaki/ascona/actions/workflows/mvn_test.yml/badge.svg)

## Student Projects

### 2019

* Oliver Brinkmann (MT): *Averaging on Lie Groups: Applications of Geodesic Averages and Biinvariant Means*
* Joel Gächter (MT): *Subdivision-Based Clothoids in Autonomous Driving*

## Nearest Neighbors

<table>
<tr>
<td>

![nearest_r2](https://user-images.githubusercontent.com/4012178/64911097-dc351300-d71d-11e9-9a92-5ce1fcd8c42f.png)

R^2

<td>

![nearest_dubins](https://user-images.githubusercontent.com/4012178/64911102-e7883e80-d71d-11e9-96d2-11273b892775.png)

Dubins

<td>

![nearest_clothoid](https://user-images.githubusercontent.com/4012178/64911109-f242d380-d71d-11e9-83cf-358a4047175b.png)

Clothoid

</tr>
</table>

### Visualization of Geodesic Averages

![deboor5](https://user-images.githubusercontent.com/4012178/51075948-ade4cc80-1692-11e9-9c9a-1e75084df796.png)

A geodesic average is the generalization of an affine combination from the Euclidean space to a non-linear space.
A geodesic average consists of a nested binary averages.
Generally, an affine combination does not have a unique expression as a geodesic average.
Instead, several geodesic averages reduce to the same affine combination when applied in Euclidean space. 

## Integration

Specify `repository` and `dependency` of the owl library in the `pom.xml` file of your maven project:

```xml
<dependencies>
  <dependency>
    <groupId>ch.alpine</groupId>
    <artifactId>ascona</artifactId>
    <version>0.0.1</version>
  </dependency>
</dependencies>

<repositories>
  <repository>
    <id>ascona-mvn-repo</id>
    <url>https://raw.github.com/datahaki/ascona/mvn-repo/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>

```

## Contributors

Jan Hakenberg, Jonas Londschien, Yannik Nager, André Stoll, Joel Gaechter

## Publications

* *What lies in the shadows? Safe and computation-aware motion planning for autonomous vehicles using intent-aware dynamic shadow regions*
by Yannik Nager, Andrea Censi, and Emilio Frazzoli,
[video](https://www.youtube.com/watch?v=3w6zQF9HOAM)

## References

* *A Generalized Label Correcting Method for Optimal Kinodynamic Motion Planning*
by Brian Paden and Emilio Frazzoli, 
[arXiv:1607.06966](https://arxiv.org/abs/1607.06966),
[video](https://www.youtube.com/watch?v=4-r6Oi8GHxc)
* *Sampling-based algorithms for optimal motion planning*
by Sertac Karaman and Emilio Frazzoli,
[IJRR11](http://ares.lids.mit.edu/papers/Karaman.Frazzoli.IJRR11.pdf)

---

The library was developed with the following objectives in mind
* trajectory design for autonomous robots
* suitable for use in safety-critical real-time systems
* implementation of theoretical concepts with high level of abstraction

<table>
<tr>
<td>

![curve_se2](https://user-images.githubusercontent.com/4012178/47631757-8f693d80-db47-11e8-9c00-7796b07c48fc.png)

Curve Subdivision

<td>

![smoothing](https://user-images.githubusercontent.com/4012178/47631759-91cb9780-db47-11e8-9dc7-a2631a144ecc.png)

Smoothing

<td>

![wachspress](https://user-images.githubusercontent.com/4012178/62423041-7c7a2f80-b6bc-11e9-874e-414ae13be3ab.png)

Wachspress

<td>

![dubinspathcurvature](https://user-images.githubusercontent.com/4012178/50681318-5d72cc80-100b-11e9-943e-e168d0463eca.png)

Dubins path curvature

</tr>
</table>

## Features

* geodesics in Lie-groups and homogeneous spaces: Euclidean space `R^n`, special Euclidean group `SE(2)`, hyperbolic half-plane `H2`, n-dimensional sphere `S^n`, ...
* parametric curves defined by control points in non-linear spaces: `GeodesicBSplineFunction`, ...
* non-linear smoothing of noisy localization data `GeodesicCenterFilter`
* Dubins path

### Visualization of Geodesic Averages

![deboor5](https://user-images.githubusercontent.com/4012178/51075948-ade4cc80-1692-11e9-9c9a-1e75084df796.png)

A geodesic average is the generalization of an affine combination from the Euclidean space to a non-linear space.
A geodesic average consists of a nested binary averages.
Generally, an affine combination does not have a unique expression as a geodesic average.
Instead, several geodesic averages reduce to the same affine combination when applied in Euclidean space. 

## Contributors

Jan Hakenberg, Oliver Brinkmann, Joel Gächter

## Publications

* *Curve Subdivision in SE(2)*
by Jan Hakenberg,
[viXra:1807.0463](http://vixra.org/abs/1807.0463),
[video](https://www.youtube.com/watch?v=2vDciaUgL4E)
* *Smoothing using Geodesic Averages*
by Jan Hakenberg,
[viXra:1810.0283](http://vixra.org/abs/1810.0283),
[video](https://www.youtube.com/watch?v=dmFO72Pigb4)
* *Curve Decimation in SE(2) and SE(3)*
by Jan Hakenberg,
[viXra:1909.0174](http://vixra.org/abs/1909.0174)

## References

* *Bi-invariant Means in Lie Groups. Application to Left-invariant Polyaffine Transformations.* by Vincent Arsigny, Xavier Pennec, Nicholas Ayache
* *Exponential Barycenters of the Canonical Cartan Connection and Invariant Means on Lie Groups* by Xavier Pennec, Vincent Arsigny
* *Lie Groups for 2D and 3D Transformations* by Ethan Eade
* *Manifold-valued subdivision schemes based on geodesic inductive averaging* by Nira Dyn, Nir Sharon
* *Power Coordinates: A Geometric Construction of Barycentric Coordinates on Convex Polytopes* by Max Budninskiy, Beibei Liu, Yiying Tong, Mathieu Desbrun
