import { Outlet } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import Footer from './Footer';

export default function MainLayout() {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'var(--background)' }}>
            {/* Top Navigation */}
            <Header />

            {/* Main Content Area */}
            <div className="container" style={{ display: 'flex', flex: 1, marginTop: '20px', alignItems: 'flex-start', gap: '20px' }}>
                {/* Left Sidebar */}
                <Sidebar />

                {/* Center Content */}
                <main style={{
                    flex: 1,
                    minHeight: '600px'
                }}>
                    <Outlet />
                </main>

                {/* Right Sidebar (Optional placeholder) */}
                {/* <aside style={{ width: '280px', flexShrink: 0 }}> ... </aside> */}
            </div>

            {/* Footer */}
            <Footer />
        </div>
    );
}
