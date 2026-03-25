// code in large parts from ChatGpt
package ch.alpine.ascona.euclid.mpm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class MPM2D {
  static final int GRID_SIZE = 50;
  static final double DX = 1.0;
  static final double DT = 0.01;
  static final double GRAVITY = -9.8;

  // ---------------- PARTICLE ----------------
  static class Particle {
    double x, y;
    double vx, vy;
    double mass;

    public Particle(double x, double y) {
      this.x = x;
      this.y = y;
      this.vx = 0;
      this.vy = 0;
      this.mass = ThreadLocalRandom.current().nextDouble();
    }
  }

  // ---------------- GRID NODE ----------------
  static class Node {
    double mass = 0;
    double vx = 0;
    double vy = 0;

    void clear() {
      mass = 0;
      vx = 0;
      vy = 0;
    }
  }

  Node[][] grid = new Node[GRID_SIZE][GRID_SIZE];
  List<Particle> particles = new ArrayList<>();

  public MPM2D() {
    // Initialize grid
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        grid[i][j] = new Node();
      }
    }
    // Create particles (a small square blob)
    for (int i = 20; i < 30; i++) {
      for (int j = 20; j < 30; j++) {
        particles.add(new Particle(i * DX, j * DX));
      }
    }
  }

  void simulate() {
    // ---------------- CLEAR GRID ----------------
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        grid[i][j].clear();
      }
    }
    // ---------------- P2G (Particle → Grid) ----------------
    for (Particle p : particles) {
      int baseX = (int) (p.x / DX);
      int baseY = (int) (p.y / DX);
      for (int i = 0; i <= 1; i++) {
        for (int j = 0; j <= 1; j++) {
          int gx = baseX + i;
          int gy = baseY + j;
          if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE)
            continue;
          double weight = 1.0; // simple (no interpolation for clarity)
          Node node = grid[gx][gy];
          node.mass += weight * p.mass;
          node.vx += weight * p.mass * p.vx;
          node.vy += weight * p.mass * p.vy;
        }
      }
    }
    // ---------------- NORMALIZE + FORCES ----------------
    for (int i = 0; i < GRID_SIZE; i++) {
      for (int j = 0; j < GRID_SIZE; j++) {
        Node node = grid[i][j];
        if (node.mass > 0) {
          node.vx /= node.mass;
          node.vy /= node.mass;
          // Apply gravity
          node.vy += DT * GRAVITY;
          // Simple boundary condition
          if (j < 2 && node.vy < 0) {
            node.vy = 0;
          }
        }
      }
    }
    // ---------------- G2P (Grid → Particle) ----------------
    for (Particle p : particles) {
      int baseX = (int) (p.x / DX);
      int baseY = (int) (p.y / DX);
      double newVX = 0;
      double newVY = 0;
      for (int i = 0; i <= 1; i++) {
        for (int j = 0; j <= 1; j++) {
          int gx = baseX + i;
          int gy = baseY + j;
          if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE)
            continue;
          double weight = 1.0;
          Node node = grid[gx][gy];
          newVX += weight * node.vx;
          newVY += weight * node.vy;
        }
      }
      p.vx = newVX;
      p.vy = newVY;
      // Update position
      p.x += DT * p.vx;
      p.y += DT * p.vy;
    }
  }
}
