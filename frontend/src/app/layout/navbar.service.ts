import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type NavbarMode = 'main' | 'productos';

@Injectable({ providedIn: 'root' })
export class NavbarService {

  private navbarSource = new BehaviorSubject<NavbarMode>('main');
  navbar$ = this.navbarSource.asObservable();

  constructor() {
    console.log('🧠 NavbarService INSTANCIADO');
  }

  cambiarNavbar(mode: NavbarMode) {
    console.log('🧭 NavbarService → cambiarNavbar:', mode);
    this.navbarSource.next(mode);
  }
}