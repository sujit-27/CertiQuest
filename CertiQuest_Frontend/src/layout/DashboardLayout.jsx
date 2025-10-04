import React from 'react'
import {useUser} from "@clerk/clerk-react"
import Navbar from '../components/Dashboard/Navbar';
import Sidemenu from '../components/Dashboard/Sidemenu';

const DashboardLayout = ({children , activeMenu}) => {

    const {user} = useUser();
    
    return (
        <>
            <div>
                {/* Navbar Component goes here*/}
                <Navbar activeMenu={activeMenu}/>
                {user && (
                    <div className="flex">
                        <div className='max-[1080px]:hidden'>
                            {/* Side Menu */}
                            <Sidemenu activeMenu={activeMenu}/>
                        </div>
                        <div className='grow mx-5'>{children}</div>
                    </div>
                )}
            </div>
        </>
    )
}

export default DashboardLayout
