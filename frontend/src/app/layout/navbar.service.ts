import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NavbarMode = 'main' | 'productos';

@Injectable({ providedIn: 'root' })
export class NavbarService {

  private navbarSource = new BehaviorSubject<NavbarMode>('main');
  navbar$ = this.navbarSource.asObservable();

  cambiarNavbar(mode: NavbarMode) {
    this.navbarSource.next(mode);
  }
}